exports.config = {
    seleniumAddress: "http://localhost:4444/wd/hub",
    // specs: ["create-feeds.js"]  // TODO: won't work if feed already exists
    specs: ["users-and-groups.js"]
};
