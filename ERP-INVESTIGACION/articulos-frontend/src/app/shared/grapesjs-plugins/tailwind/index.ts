import type { Plugin } from "grapesjs";
import { blocks } from "./blocks";


export interface PluginOptions {
    loadBlocks?: boolean;
    bloques?: any[];
}

export const plugin: Plugin<PluginOptions> = (editor, opts = {}) => {
    const options = {
        ...{
            loadBlocks: true,
        },
        ...opts,
    };
    if (options.loadBlocks) blocks(editor, options);
};

export default plugin;
