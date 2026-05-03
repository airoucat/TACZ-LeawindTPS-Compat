from __future__ import annotations

import argparse
import json
import os
import stat
import subprocess
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]
HOOK_DIR = REPO_ROOT / ".githooks"


def ensure_graphify() -> None:
    try:
        import graphify  # noqa: F401
        return
    except ImportError:
        print("[graphify setup] graphify 未安装，使用当前 Python 自动安装 graphifyy ...")
        subprocess.run(
            [sys.executable, "-m", "pip", "install", "--user", "graphifyy"],
            cwd=REPO_ROOT,
            check=True,
        )
        import graphify  # noqa: F401


def ensure_hook_permissions() -> None:
    if os.name == "nt" or not HOOK_DIR.exists():
        return
    for path in HOOK_DIR.iterdir():
        if path.is_file():
            mode = path.stat().st_mode
            path.chmod(mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)


def ensure_codex_local_hook() -> None:
    subprocess.run(
        [sys.executable, "-m", "graphify", "codex", "install"],
        cwd=REPO_ROOT,
        check=True,
    )
    if os.name == "nt":
        ensure_codex_windows_hook_compatibility()


def ensure_codex_windows_hook_compatibility() -> None:
    hooks_path = REPO_ROOT / ".codex" / "hooks.json"
    if not hooks_path.exists():
        return

    hook_script = REPO_ROOT / "scripts" / "dev" / "codex_graphify_pre_tool_hook.ps1"
    command = f'powershell -NoProfile -ExecutionPolicy Bypass -File "{hook_script}"'

    existing = json.loads(hooks_path.read_text(encoding="utf-8"))
    pre_tool = existing.setdefault("hooks", {}).setdefault("PreToolUse", [])
    existing["hooks"]["PreToolUse"] = [h for h in pre_tool if "graphify" not in str(h).lower()]
    existing["hooks"]["PreToolUse"].append(
        {
            "matcher": "Bash",
            "hooks": [
                {
                    "type": "command",
                    "command": command,
                }
            ],
        }
    )
    hooks_path.write_text(json.dumps(existing, indent=2) + "\n", encoding="utf-8")


def configure_hooks_path() -> None:
    subprocess.run(
        ["git", "config", "--local", "core.hooksPath", ".githooks"],
        cwd=REPO_ROOT,
        check=True,
    )


def rebuild_code_graph(reason: str) -> int:
    ensure_graphify()
    from graphify.watch import _rebuild_code

    print(f"[graphify setup] 触发代码图重建，来源: {reason}")
    ok = _rebuild_code(REPO_ROOT)
    if ok:
        out = REPO_ROOT / "graphify-out"
        out.mkdir(exist_ok=True)
        (out / ".graphify_python").write_text(sys.executable, encoding="utf-8")
        return 0
    return 1


def install_local_automation(skip_rebuild: bool) -> int:
    ensure_graphify()
    ensure_hook_permissions()
    configure_hooks_path()
    ensure_codex_local_hook()

    print("[graphify setup] 已将 Git hooks 切换到仓库内 .githooks/")
    print("[graphify setup] commit / checkout / merge / rebase 后会自动重建本地代码图")

    graph_json = REPO_ROOT / "graphify-out" / "graph.json"
    if skip_rebuild:
        return 0
    if graph_json.exists():
        print("[graphify setup] 已存在 graphify-out/graph.json，跳过首轮重建")
        return 0
    return rebuild_code_graph("initial-install")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Install repo-local Graphify automation for TACZ-LeawindTPS-Compat."
    )
    subparsers = parser.add_subparsers(dest="command")

    install_parser = subparsers.add_parser("install", help="configure repo-local hooks and local Codex hook")
    install_parser.add_argument(
        "--skip-rebuild",
        action="store_true",
        help="configure hooks only, do not build graphify-out/graph.json on first install",
    )

    rebuild_parser = subparsers.add_parser("rebuild", help="rebuild code graph only")
    rebuild_parser.add_argument(
        "--reason",
        default="manual",
        help="human-readable trigger label for logs",
    )

    parser.set_defaults(command="install", skip_rebuild=False)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.command == "rebuild":
        return rebuild_code_graph(args.reason)
    return install_local_automation(args.skip_rebuild)


if __name__ == "__main__":
    raise SystemExit(main())
