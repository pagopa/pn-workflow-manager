/**
 * Legge una variabile d'ambiente in formato JSON.
 * Se la variabile non esiste o il JSON non è valido, usa il valore di default fornito.
 */
function parseEnvJson(envVarName, defaultValue) {
    const rawValue = process.env[envVarName];
    if (!rawValue) return defaultValue;
    try {
        return JSON.parse(rawValue);
    } catch (e) {
        console.warn(`[CONFIG WARNING] Impossibile parsare la ENV ${envVarName}, uso il valore di default.`);
        return defaultValue;
    }
}

module.exports = {
    parseEnvJson
};