package de.uni_koeln.arachne.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.jdbc.Statement;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;

@Repository("CatalogEntryDao")
public class CatalogEntryDao extends SQLDao {
		
	@Autowired
	private transient ArachneEntityDao arachneEntityDao;
	
	private transient UserRightsService userRightsService;
	
	// needed to inject mock for testing - it should work without this setter but it does not
	@Autowired
	public void setUserRightsService(final UserRightsService userRightsService) {
		this.userRightsService = userRightsService;
	}
	
	public CatalogEntry getById(final long catalogEntryId) {
		return getById(catalogEntryId, false, -1, 0);
	}
	
	/**
	 * Retrieves a CatalogEntry from the DB. The parameters are used to restrict the children.
	 * @param catalogEntryId The entries id.
	 * @param full If all children of all children should be retrieved or only the direct children of the entry.
	 * @param limit If <code>full = false</code> then limit restricts the number of direct children to the desired 
	 * value (-1 for no limit).
	 * @param offset If <code>full = false</code> and <code>limit > 0</code> then offset gives an offset into the 
	 * direct children list.
	 * @return The CatalogEntry with the given id.
	 */
	@Transactional(readOnly=true)
	public CatalogEntry getById(final long catalogEntryId, final boolean full, final int limit, final int offset) {
		final String sqlQuery = "SELECT * from catalog_entry WHERE id = " + catalogEntryId;
		if (full) {
			final CatalogEntry result = queryForObject(sqlQuery, this::mapCatalogEntryFull);
			return result;
		} else {
			if (limit == 0) {
				return queryForObject(sqlQuery, this::mapCatalogEntryNoChilds);
			}
			final CatalogEntry result = queryForObject(sqlQuery, this::mapCatalogEntryDirectChildsOnly);
			// This should be fast enough as limiting at query time did not work reliably for h2
			if (offset > 0) {
				final List<CatalogEntry> children = result.getChildren();
				final int childCount = children == null ? 0 : children.size();
				if (offset < childCount) {
					children.subList(0, offset).clear();
				}
			}
			if (limit > 0) {
				final List<CatalogEntry> children = result.getChildren();
				if (children != null) {
					final int childCount = children == null ? 0 : children.size();
					if (childCount > limit) {
						children.subList(limit, childCount).clear();
					}
				}
			}
			return result;
		}
	}
	
	@Transactional(readOnly=true)
	public List<CatalogEntry> getChildrenByParentId(final long parentId, final RowMapper<CatalogEntry> rowMapper) {
		final String sqlQuery = "SELECT * from catalog_entry WHERE parent_id = " + parentId + " ORDER BY index_parent";
		List<CatalogEntry> result = query(sqlQuery, rowMapper);
		if (result != null && result.isEmpty()) {
			result = null;
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public int getChildrenSizeByParentId(final long parentId) {
		final String sqlQuery = "SELECT COUNT(*) from catalog_entry WHERE parent_id = " + parentId;
		final Integer result = queryForInt(sqlQuery);
		return result;
	}
	
	/**
	 * Retrieves a list of <code>CatalogEntries</code> that are connected to an Arachne entity.
	 * @param entityId The arachne entity id of interest.
	 * @return A list of catalog entries.
	 */
	@Transactional(readOnly=true)
	public List<CatalogEntry> getByEntityId(final long entityId) {
		final String sqlQuery = "SELECT * from catalog_entry WHERE arachne_entity_id = " + entityId;
		List<CatalogEntry> result = query(sqlQuery, this::mapCatalogEntryNoChilds);
		return result;
	}
		
	@Transactional
	public CatalogEntry saveCatalogEntry(final CatalogEntry newCatalogEntry) throws DataAccessException {
		final String catalogIdQuery = "SELECT id "
				+ "FROM catalog "
				+ "WHERE id = " 
				+ newCatalogEntry.getCatalogId()
				+ userRightsService.getSQL("catalog");
		final Long catalogId = queryForLong(catalogIdQuery);
		
		if (catalogId != null 
				&& (newCatalogEntry.getArachneEntityId() == null
				|| arachneEntityDao.getByEntityID(newCatalogEntry.getArachneEntityId()) != null)) {
			final Long parentId = newCatalogEntry.getParentId();
			if (parentId != null) {
				newCatalogEntry.setPath(getById(parentId).getPath() + '/' + newCatalogEntry.getParentId());
				int maxIndex = getChildrenSizeByParentId(parentId);
				if (newCatalogEntry.getIndexParent() > maxIndex) {
					newCatalogEntry.setIndexParent(maxIndex);
				}

				update(con -> {
					final String sql = "UPDATE catalog_entry "
							+ "SET index_parent = index_parent + 1 "
							+ "WHERE (index_parent >= ? AND parent_id = ?)";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setLong(1, newCatalogEntry.getIndexParent());
					ps.setObject(2, newCatalogEntry.getParentId(), Types.BIGINT);
					return ps;
				});
			} else {
				newCatalogEntry.setPath(String.valueOf(newCatalogEntry.getCatalogId()));
				final String sql = "SELECT id FROM catalog_entry WHERE (catalog_id = " + newCatalogEntry.getCatalogId() 
						+ " AND parent_id IS NULL)";
				final Long catalogRootEntryId = queryForLong(sql);
				if (catalogRootEntryId != null) {
					delete(catalogRootEntryId);
				}
				newCatalogEntry.setIndexParent(0);
			}
			newCatalogEntry.setId(updateReturnKey(con -> {
				final String sql = "INSERT INTO catalog_entry "
						+ "(catalog_id, parent_id, arachne_entity_id, index_parent, path, label, text, creation) "
						+ "VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, NOW())";
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, newCatalogEntry.getCatalogId());
				ps.setObject(2, newCatalogEntry.getParentId(), Types.BIGINT);
				ps.setObject(3, newCatalogEntry.getArachneEntityId(), Types.BIGINT);
				ps.setInt(4, newCatalogEntry.getIndexParent());
				ps.setString(5, newCatalogEntry.getPath());
				ps.setString(6, newCatalogEntry.getLabel());
				ps.setString(7, newCatalogEntry.getText());
				return ps;
			}));

			return newCatalogEntry;
		}
		return null;
	}
		
	@Transactional
	public CatalogEntry updateCatalogEntry(final CatalogEntry newCatalogEntry) throws DataAccessException {
		
		// check if parent exists
		CatalogEntry parent = null;
		if (newCatalogEntry.getParentId() != null) {
			parent = getById(newCatalogEntry.getParentId());
			if (parent == null) {
				return null;
			}
		}
		
		if (parent != null) {
			if (parent.getCatalogId().equals(newCatalogEntry.getCatalogId())) {
				final CatalogEntry oldEntry = getById(newCatalogEntry.getId());
				newCatalogEntry.setPath(oldEntry.getPath());
				if (oldEntry.getIndexParent() != newCatalogEntry.getIndexParent() || 
						oldEntry.getParentId() != newCatalogEntry.getParentId()) {

					int maxIndex = getChildrenSizeByParentId(newCatalogEntry.getParentId());
					if (oldEntry.getParentId() != newCatalogEntry.getParentId()) {
						newCatalogEntry.setPath(parent.getPath() + '/' + newCatalogEntry.getParentId());
						if (newCatalogEntry.getIndexParent() > maxIndex) {
							newCatalogEntry.setIndexParent(maxIndex);
						}	
					} else {
						if (newCatalogEntry.getIndexParent() > maxIndex) {
							newCatalogEntry.setIndexParent(maxIndex - 1);
						}
					}

					update(con -> {
						final String sql = "UPDATE catalog_entry "
								+ "SET index_parent = index_parent - 1 "
								+ "WHERE (index_parent > ? AND parent_id = ?)";
						PreparedStatement ps = con.prepareStatement(sql);
						ps.setLong(1, oldEntry.getIndexParent());
						ps.setLong(2, oldEntry.getParentId());
						return ps;
					});

					update(con -> {
						final String sql = "UPDATE catalog_entry "
								+ "SET index_parent = index_parent + 1 "
								+ "WHERE (index_parent >= ? AND parent_id = ?)";
						PreparedStatement ps = con.prepareStatement(sql);
						ps.setLong(1, newCatalogEntry.getIndexParent());
						ps.setLong(2, newCatalogEntry.getParentId());
						return ps;
					});
				}

				update(con -> {
					final String sql = "UPDATE catalog_entry SET "
							+ "catalog_id = ?, parent_id = ?, arachne_entity_id = ?, index_parent = ?, path = ?, label = ?, "
							+ "text = ? "
							+ "WHERE id = ?";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setLong(1, newCatalogEntry.getCatalogId());
					ps.setObject(2, newCatalogEntry.getParentId(), Types.BIGINT);
					ps.setObject(3, newCatalogEntry.getArachneEntityId(), Types.BIGINT);
					ps.setInt(4, newCatalogEntry.getIndexParent());
					ps.setString(5, newCatalogEntry.getPath());
					ps.setString(6, newCatalogEntry.getLabel());
					ps.setString(7, newCatalogEntry.getText());
					ps.setLong(8, newCatalogEntry.getId());
					return ps;
				});
				return newCatalogEntry;
			}
			return null;
		} else {
			final CatalogEntry oldEntry = getById(newCatalogEntry.getId());
			if (oldEntry != null) {
				newCatalogEntry.setPath(oldEntry.getPath());
				newCatalogEntry.setTotalChildren(oldEntry.getTotalChildren());
				
				update(con -> {
					final String sql = "UPDATE catalog_entry SET "
							+ "arachne_entity_id = ?, label = ?, text = ? "
							+ "WHERE id = ?";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setObject(1, newCatalogEntry.getArachneEntityId(), Types.BIGINT);
					ps.setString(2, newCatalogEntry.getLabel());
					ps.setString(3, newCatalogEntry.getText());
					ps.setLong(4, newCatalogEntry.getId());
					return ps;
				});
				return newCatalogEntry;
			}
			return null;
		}
	}
	
	/**
	 * Removes a catalog entry and all its children.
	 * @param catalogEntryId The catalog entries id.
	 * @return <code>true</code> if the entry was successfully deleted.
	 * @throws DataAccessException
	 */
	@Transactional
	public boolean delete(final long catalogEntryId) throws DataAccessException {
		final CatalogEntry catalogEntry = getById(catalogEntryId);
		
		update(con -> {
			final String sql = "UPDATE catalog_entry "
					+ "SET index_parent = index_parent - 1 "
					+ "WHERE (index_parent > ? AND parent_id = ?)";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, catalogEntry.getIndexParent());
			ps.setObject(2, catalogEntry.getParentId(), Types.BIGINT);
			return ps;
		});
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		if (children != null) {
			for (CatalogEntry child : children) {
				delete(child.getId());
			}
		}
		
		final int updatedRows = update(con -> {
			final String sql = "DELETE FROM catalog_entry WHERE id = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, catalogEntryId);
			return ps;
		});
		
		return updatedRows == 1;
	}

	/**
	 * Maps a SQL result set to the base fields of a catalog entry. It does not care about children at all.
	 * @param rs The SQL result set.
	 * @param rowNum The row number.
	 * @return The mapped <code>CatalogEntry</code>.
	 * @throws SQLException
	 */
	public CatalogEntry mapBaseCatalogEntry (ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(rs.getLong("id"));
		catalogEntry.setCatalogId(rs.getLong("catalog_id"));
		final long parentId = rs.getLong("parent_id");
		catalogEntry.setParentId(rs.wasNull() ? null : parentId);
		final long arachneEntityId = rs.getLong("arachne_entity_id");
		catalogEntry.setArachneEntityId(rs.wasNull() ? null : arachneEntityId);
		catalogEntry.setIndexParent(rs.getInt("index_parent"));
		catalogEntry.setPath(rs.getString("path"));
		catalogEntry.setLabel(rs.getString("label"));
		catalogEntry.setText(rs.getString("text"));
		return catalogEntry;
	}
	
	/**
	 * Maps a SQL result set to a catalog entry. Direct children are included.
	 * @param rs The SQL result set.
	 * @param rowNum The row number.
	 * @return The mapped <code>CatalogEntry</code>.
	 * @throws SQLException
	 */
	public CatalogEntry mapCatalogEntryDirectChildsOnly(ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = mapBaseCatalogEntry(rs, rowNum);
        final List<CatalogEntry> children = getChildrenByParentId(catalogEntry.getId(), this::mapCatalogEntryNoChilds);
        setTotalChildren(catalogEntry, children);
		return catalogEntry;
	}
	
	/**
	 * Maps a SQL result set to a catalog entry. All children are included.
	 * @param rs The SQL result set.
	 * @param rowNum The row number.
	 * @return The mapped <code>CatalogEntry</code>.
	 * @throws SQLException
	 */
	public CatalogEntry mapCatalogEntryFull(ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = mapBaseCatalogEntry(rs, rowNum);
        final List<CatalogEntry> children = getChildrenByParentId(catalogEntry.getId(), this::mapCatalogEntryFull);
        setTotalChildren(catalogEntry, children);
		return catalogEntry;
	}
	
	/**
	 * Maps a SQL result set to a catalog entry. Children are not included but the <code>hasChildren</code> property 
	 * is set.
	 * @param rs The SQL result set.
	 * @param rowNum The row number.
	 * @return The mapped <code>CatalogEntry</code>.
	 * @throws SQLException
	 */
	public CatalogEntry mapCatalogEntryNoChilds(ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = mapBaseCatalogEntry(rs, rowNum);
		catalogEntry.setTotalChildren(getChildrenSizeByParentId(catalogEntry.getId()));
		return catalogEntry;
	}

    private void setTotalChildren(CatalogEntry catalogEntry, List<CatalogEntry> children) {
        catalogEntry.setChildren(children);
        final int childCount = (children != null) ? children.size() : 0;
        catalogEntry.setTotalChildren(childCount);
    }
}
