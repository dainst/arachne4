/**
 * @author: Simon Hohl
 */

export default function () {
    return {
        restrict: 'E',
        scope: {
            src: '@'
        },
        template: require('./con10t-include.html')
    }
};
