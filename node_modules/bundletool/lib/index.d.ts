/// <reference types="node" />
import { Command } from "./types";
declare const _default: (command: Command, args?: string[]) => Promise<import("child_process").SpawnSyncReturns<string>>;
export default _default;
