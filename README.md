# BonFireItemDamagedPatch

[English](#english) | [简体中文](#简体中文)

BonFireItemDamagedPatch is Bonfire's maintained item-condition patch branch.

BonFireItemDamagedPatch 是 Bonfire 维护中的物品成色系统补丁分支。

---

## English

BonFireItemDamagedPatch is the maintained Bonfire patch branch for item condition, durability presentation, lore cleanup, and ItemsAdder-facing runtime behavior.

### What It Does

- Applies condition-based behavior to gameplay items.
- Integrates with ItemsAdder content definitions.
- Uses NBT-backed state to keep item condition consistent.
- Focuses on lore deduplication and cleaner runtime presentation.

### Repository Layout

- `src/`: plugin source code
- `build.ps1`: local build helper
- `build/`: local build output, excluded from Git release tracking

### Build

```powershell
.\build.ps1
```

### License

This repository currently uses the `Bonfire Non-Commercial Source License 1.0`.
See [LICENSE](LICENSE) for the exact terms.

---

## 简体中文

BonFireItemDamagedPatch 是 Bonfire 持续维护中的物品成色系统补丁分支，重点处理耐久展示、状态一致性与 lore 清理。

### 它的作用

- 为游戏物品附加基于成色或损耗的运行时行为。
- 与 ItemsAdder 内容定义联动。
- 使用基于 NBT 的状态记录保持物品成色一致。
- 重点改善 lore 去重与展示层可读性。

### 仓库结构

- `src/`：插件源码
- `build.ps1`：本地构建脚本
- `build/`：本地构建输出，不纳入发布源码

### 构建方式

```powershell
.\build.ps1
```

### 授权

本仓库当前采用 `Bonfire Non-Commercial Source License 1.0`。
具体条款见 [LICENSE](LICENSE)。
