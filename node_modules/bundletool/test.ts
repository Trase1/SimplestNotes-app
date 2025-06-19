import bundletool from "./src/index"

async function test() {
    const r = await bundletool("version", []);
    console.log("Status:", r.status);
    console.log("Version:", r.stdout.toString());
}

// Run a simple manual test.
test();
