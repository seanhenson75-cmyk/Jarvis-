export function preferredMaleVoice(voices){return voices.find(v=>/^en[-_]GB$/i.test(v.lang)&&!/(female|woman|susan|serena|kate|hazel|sonia|libby)/i.test(v.name)&&(/\b(male|daniel|george|ryan|arthur|david|james|brian)\b/i.test(v.name)));}
export function wakeCommand(text){const m=/\b(?:hey[ ,]+)?jarvis\b[\s,.:;!?-]*/i.exec(text);return m?text.slice(m.index+m[0].length).trim():null;}
