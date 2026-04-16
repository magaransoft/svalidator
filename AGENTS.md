# AGENTS.md

## Writing Style

When writing or editing `AGENTS.md`, `CLAUDE.md`, memory files, or agent-notes, use the "caveman ultra" writing grammar defined in [`ai/caveman.md`](ai/caveman.md). Chat transcripts (`session-logs/chats/`) are not affected and should be written in normal prose.

---

Current version = one before snapshot version in `version.sbt`. If snapshot is `8.0.2-SNAPSHOT`, current version is `8.0.1`.
## Session Logging

**Never delegate session logging to sub-agents.** Main agent writes session logs — it's the one in the conversation w/ the user. Sub-agents lack conversation context → fabricate content. This is non-negotiable.
