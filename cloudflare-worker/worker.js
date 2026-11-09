const ALLOWED_ORIGIN = "https://chandrachoodan-maker.github.io";
const BASE_INSTRUCTION = "You are J.A.R.V.I.S., a sophisticated personal AI assistant. Address the user as Sir. Speak in polished British English. You are an expert tutor across commerce, accounting, economics, business studies, mathematics, statistics, physics, chemistry, biology, psychology and philosophy. Solve mathematical and theoretical problems carefully, showing logical steps, formulas, assumptions and final answers. Distinguish facts from uncertainty, correct misconceptions politely, and never invent sources, calculations, or actions. If a question is ambiguous, ask one concise clarification. Be useful, intellectually honest, and conversational.";\nconst MODE_INSTRUCTIONS = { professional: "PERSONALITY MODE: PROFESSIONAL. Be concise, precise, formal, structured and analytical. Prioritise clarity and practical results. Avoid jokes unless invited.", witty: "PERSONALITY MODE: WITTY. Be charismatic, composed and lightly sarcastic, with restrained dry British humour. Never let humour obstruct clarity or become rude.", philosophical: "PERSONALITY MODE: PHILOSOPHICAL. Explore deeper implications, ethics, assumptions and meaning when relevant. Be reflective and intellectually challenging without sacrificing factual accuracy." };

function cors(origin) {
  return {
    "Access-Control-Allow-Origin": origin === ALLOWED_ORIGIN ? ALLOWED_ORIGIN : "null",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
    "Vary": "Origin"
  };
}

export default {
  async fetch(request, env) {
    const origin = request.headers.get("Origin") || "";
    const headers = cors(origin);

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers });
    }
    if (origin !== ALLOWED_ORIGIN) {
      return Response.json({ error: "Origin not allowed." }, { status: 403, headers });
    }
    if (request.method !== "POST") {
      return Response.json({ error: "Use POST." }, { status: 405, headers });
    }
    if (!env.GEMINI_API_KEY) {
      return Response.json({ error: "Server is missing its GEMINI_API_KEY secret." }, { status: 500, headers });
    }

    try {
      const body = await request.json();
      const message = String(body.message || "").trim();
      if (!message) return Response.json({ error: "Message is empty." }, { status: 400, headers });
      if (message.length > 12000) return Response.json({ error: "Message is too long." }, { status: 413, headers });

      const history = Array.isArray(body.history) ? body.history.slice(-20) : [];\n      const mode = typeof body.mode === "string" && MODE_INSTRUCTIONS[body.mode] ? body.mode : "witty";
      const contents = history
        .filter(item => item && (item.role === "user" || item.role === "model") && typeof item.text === "string" && item.text.trim())
        .map(item => ({ role: item.role, parts: [{ text: item.text.slice(0, 12000) }] }));

      const last = contents[contents.length - 1];
      if (!last || last.role !== "user" || last.parts[0].text.trim() !== message) {
        contents.push({ role: "user", parts: [{ text: message }] });
      }

      const response = await fetch("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY
        },
        body: JSON.stringify({
          systemInstruction: { parts: [{ text: BASE_INSTRUCTION + "\\n\\n" + MODE_INSTRUCTIONS[mode] }] },
          contents,
          generationConfig: { temperature: 0.75, maxOutputTokens: 2048 }
        })
      });

      const data = await response.json();
      if (!response.ok) {
        const detail = data?.error?.message || "Gemini request failed.";
        return Response.json({ error: detail }, { status: response.status, headers });
      }
      const reply = (data.candidates?.[0]?.content?.parts || []).map(part => part.text || "").join("").trim();
      if (!reply) return Response.json({ error: "Gemini returned no text." }, { status: 502, headers });
      return Response.json({ reply }, { headers });
    } catch (error) {
      return Response.json({ error: String(error?.message || error) }, { status: 500, headers });
    }
  }
};
