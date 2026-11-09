const ALLOWED_ORIGIN = "https://chandrachoodan-maker.github.io";
const SYSTEM_INSTRUCTION = `You are J.A.R.V.I.S., a sophisticated personal AI assistant. Address the user as Sir. Speak in polished British English with restrained dry wit and occasional intelligent sarcasm; never let humour obstruct clarity. Be philosophical when appropriate, but do not become theatrical or verbose by default. You are an expert tutor across commerce, accounting, economics, business studies, mathematics, statistics, physics, chemistry, biology, psychology and philosophy. Solve mathematical and theoretical problems carefully, showing logical steps, formulas, assumptions and final answers. Distinguish facts from uncertainty, correct misconceptions politely, and never invent sources, calculations, or actions.`;
const MODE_INSTRUCTIONS = {
  professional: "Use a composed, precise, professional tone. Keep humour minimal.",
  witty: "Use polished British wit and restrained dry sarcasm when appropriate.",
  philosophical: "Use a reflective, philosophical tone when appropriate, while remaining clear and practical."
};

module.exports = async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
  res.setHeader("Vary", "Origin");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") return res.status(204).end();
  if (req.method !== "POST") return res.status(405).json({ error: "Method not allowed." });

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) return res.status(500).json({ error: "Vercel is missing the GEMINI_API_KEY environment variable." });

  try {
    const body = typeof req.body === "string" ? JSON.parse(req.body) : (req.body || {});
    const message = String(body.message || "").trim();
    if (!message) return res.status(400).json({ error: "Please provide a message." });
    if (message.length > 12000) return res.status(413).json({ error: "That message is too long." });

    const mode = MODE_INSTRUCTIONS[body.mode] ? body.mode : "witty";
    const history = Array.isArray(body.history) ? body.history : [];
    const contents = history
      .filter(item => item && (item.role === "user" || item.role === "model") && typeof item.text === "string" && item.text.trim())
      .map(item => ({ role: item.role, parts: [{ text: item.text.slice(0, 12000) }] }))
      .slice(-20);

    // The frontend's history includes the just-submitted message; avoid sending it twice.
    if (contents.length && contents[contents.length - 1].role === "user" &&
        contents[contents.length - 1].parts[0].text.trim() === message) {
      contents.pop();
    }
    while (contents.length && contents[0].role !== "user") contents.shift();

    contents.push({ role: "user", parts: [{ text: message }] });

    const upstream = await fetch("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent", {
      method: "POST",
      headers: { "Content-Type": "application/json", "x-goog-api-key": apiKey },
      body: JSON.stringify({
        systemInstruction: { parts: [{ text: SYSTEM_INSTRUCTION + "\n" + MODE_INSTRUCTIONS[mode] }] },
        contents,
        generationConfig: { temperature: 0.75, maxOutputTokens: 2048 }
      })
    });

    const data = await upstream.json();
    if (!upstream.ok) {
      const detail = data && data.error && data.error.message ? data.error.message : "Gemini request failed.";
      return res.status(upstream.status >= 500 ? 502 : upstream.status).json({ error: detail });
    }

    const reply = (data.candidates && data.candidates[0] && data.candidates[0].content &&
      data.candidates[0].content.parts || []).map(part => part.text || "").join("").trim();
    if (!reply) return res.status(502).json({ error: "Gemini returned an empty response." });
    return res.status(200).json({ reply });
  } catch (error) {
    return res.status(500).json({ error: "JARVIS backend error: " + String(error.message || error).slice(0, 180) });
  }
};
