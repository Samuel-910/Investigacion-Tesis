
export const purify = (html: string, css: string, opts: any = {}, callback: any = null) => {
    if (callback) callback(css);
    return css;
};
