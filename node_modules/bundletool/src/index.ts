import { spawnSync } from "child_process";
import commandExists from 'command-exists';

import { Command } from "./types";
import { BUNDLETOOL_FILE_PATH } from "../constants";


let checkedJava = false;
function checkJavaInPath() {
  if (!commandExists.sync('java')) {
    throw new Error("Java is required for bundletool");
  }
  checkedJava = true;
}

export default async (command: Command, args: string[] = []) => {
  if (!checkedJava) {
    checkJavaInPath();
  }
  return spawnSync("java", ["-jar", BUNDLETOOL_FILE_PATH, command, ...args]);
};
