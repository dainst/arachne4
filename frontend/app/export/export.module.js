import DownloadController from './download.controller.js';
import './download-modal.scss';

export default angular.module('arachne.export', [])
    .controller('DownloadController', ['$scope', '$uibModalInstance', '$http', '$filter', 'arachneSettings', 'downloadUrl', 'downloadParams', 'transl8', 'language', DownloadController])
;
