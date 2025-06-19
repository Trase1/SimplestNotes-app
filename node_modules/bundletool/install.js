// @ts-check
const { spawnSync } = require("child_process");
const { existsSync, mkdirSync, createWriteStream } = require("fs");
const axios = require("axios");
const path = require("path");
const pkg = require("./package.json");
const mkdir = require("mkdirp");
const fs = require("fs");
const process = require("process");

const {
  BUNDLETOOL_VERSION,
  DEFAULT_BASE_DIRECTORY,
  BUNDLETOOL_FILE_PATH,
  BUNDLETOOL_FILE_NAME,
} = require("./constants");
const BUNDLETOOL_URL = `https://github.com/google/bundletool/releases/download/${BUNDLETOOL_VERSION}/bundletool-all-${BUNDLETOOL_VERSION}.jar`;

// process.env.npm_config_cache is not set if installing via yarn, and there's no
// other env variable that determines the caching dir for yarn. so we'll need to fetch
// yarn cache via terminal  (couldn't find another way to get yarn cache dir)
//
// if installing through yarn npm_config_user_agent will have "yarn/<version>" (among other things)
// if installing through npm, this variable only references npm / node.
const CACHE_DIR = process.env.npm_config_user_agent.includes("yarn")
  ? spawnSync("yarn", ["cache", "dir"]).stdout.toString()
  : process.env.npm_config_cache;

const log = (message) => {
  const logLevel = process.env.npm_config_loglevel;
  const logLevelDisplay = ["silent", "error", "warn"].includes(logLevel);

  if (!logLevelDisplay) {
    console.log(message);
  }
};

const downloadBinary = () => {
  return new Promise(async (resolve, reject) => {
    log(
      `Downloading bundletool ${BUNDLETOOL_VERSION} to ${BUNDLETOOL_FILE_PATH}`,
    );

    const { data } = await axios.get(BUNDLETOOL_URL, {
      responseType: "stream",
      headers: {
        "Cache-Control": "max-age=0",
        Connection: "keep-alive",
        "User-Agent":
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36",
      },
    });

    data.pipe(createWriteStream(BUNDLETOOL_FILE_PATH));

    data.on("end", () => {
      const cachePath = getBinaryCachePath();
      if (cachePath) {
        const cachedBinary = path.join(cachePath, BUNDLETOOL_FILE_NAME);

        console.log("Caching binary to", cachedBinary);
        try {
          mkdir.sync(path.dirname(cachePath));
          fs.createReadStream(BUNDLETOOL_FILE_PATH)
            .pipe(fs.createWriteStream(cachedBinary))
            .on("error", function (err) {
              console.log("Failed to cache binary:", err);
            });
        } catch (err) {
          console.log("Failed to cache binary:", err);
        }
      }
      resolve(BUNDLETOOL_FILE_PATH);
    });
    data.on("error", (error) => {
      reject(error);
    });

    return BUNDLETOOL_FILE_PATH;
  });
};

function getBinaryCachePath() {
  const cachePath = path.join(CACHE_DIR, pkg.name, pkg.version);

  try {
    mkdir.sync(cachePath);
    return cachePath;
  } catch (e) {
    console.error("Cache path not found! " + cachePath);
  }

  return "";
}

function getCachedBinary() {
  const cachePath = path.join(CACHE_DIR, pkg.name, pkg.version);
  const cacheBinary = path.join(cachePath, BUNDLETOOL_FILE_NAME);

  if (fs.existsSync(cacheBinary)) {
    return cacheBinary;
  }

  return "";
}

const setupBundletool = async () => {
  // bundletool already downloaded and ready to go
  if (existsSync(BUNDLETOOL_FILE_PATH)) {
    console.log("Binary already present, proceed");
    return;
  }

  const binaryPath = getCachedBinary();

  if (!existsSync(DEFAULT_BASE_DIRECTORY)) {
    mkdirSync(DEFAULT_BASE_DIRECTORY);
  }

  // check for existing binary, if found, copy it over
  if (fs.existsSync(binaryPath)) {
    fs.copyFileSync(binaryPath, BUNDLETOOL_FILE_PATH);
    console.log("Cached binary found at", binaryPath);
    return;
  }

  // cached binary not found, proceed to download
  await downloadBinary();
};

setupBundletool();
