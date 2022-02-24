export default function () {
    return function (string) {
        return string.replace(/\//g, '\\/');
    }
};
