export default function ($timeout) {
    return {
        link: function (scope, element) {

            $timeout(function () {
                element[0].focus();
            }, 300);
        }
    };
};
