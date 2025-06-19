const { resolve, join } = require("path");

const BUNDLETOOL_VERSION = "1.0.0";
const BIN_PATH = "./.bundletool";
const DEFAULT_BASE_DIRECTORY = resolve(__dirname, BIN_PATH);
const BUNDLETOOL_FILE_NAME = `bundletool-${BUNDLETOOL_VERSION}.jar`;
const BUNDLETOOL_FILE_PATH = join(
  DEFAULT_BASE_DIRECTORY,
  BUNDLETOOL_FILE_NAME,
);

module.exports = {
  BUNDLETOOL_VERSION,
  BIN_PATH,
  DEFAULT_BASE_DIRECTORY,
  BUNDLETOOL_FILE_PATH,
  BUNDLETOOL_FILE_NAME
};
