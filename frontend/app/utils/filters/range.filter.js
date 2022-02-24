export default function() {
    return function(count) {
        var result = [];
        for (var i = 0; i < count; i++) {
            result.push(i);
        }
        return result;
    };
};
