$ErrorActionPreference = "Stop"

try {
    $repoRoot = Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")
    $graphJson = Join-Path $repoRoot "graphify-out\graph.json"

    if (Test-Path -LiteralPath $graphJson) {
        $payload = @{
            hookSpecificOutput = @{
                hookEventName = "PreToolUse"
                additionalContext = "graphify: Knowledge graph exists. Read graphify-out/GRAPH_REPORT.md for god nodes and community structure before searching raw files."
            }
        }
        $payload | ConvertTo-Json -Compress -Depth 4
    }
}
catch {
    # PreToolUse context is advisory; never block Codex tools because graphify metadata is unavailable.
}

exit 0
