export default function () {
    return function (input) {
        return (!!input) ? input.charAt(0).toLowerCase() + input.substr(1) : '';
    }
};
