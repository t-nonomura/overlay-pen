# Feasibility Study

## Executive Summary

このアプリは Android ネイティブで実装可能。  
ただし Android 12 以降の overlay touch 制約により、描画を残したまま下位アプリ操作へ戻るときは alpha 制約を受ける。

## Current Repository Status

- Kotlin / Android 実装がリポジトリに存在する
- `TYPE_APPLICATION_OVERLAY`、foreground service、フローティングボタン、描画キャンバス、ツールパネルまで実装済み
- Passive Annotation では overlay alpha `0.78` を採用している

## Key Platform Constraints

### 1. Overlay Permission Is Mandatory

`TYPE_APPLICATION_OVERLAY` を使うには `SYSTEM_ALERT_WINDOW` が必要。

### 2. Overlay Is Not Truly Above Everything

overlay は常に最前面とは限らず、system UI や一部の画面では制約を受ける。

### 3. Android 12+ Blocks Unsafe Touch Pass-Through

Android 12 以降、touch pass-through は overlay の不透明度制約を受ける。  
そのため V1 では Passive Annotation 用 alpha を `0.78` に固定する。

### 4. Some Apps Can Hide Third-Party Overlays

一部アプリは `HIDE_OVERLAY_WINDOWS` により third-party overlay を非表示にできる。

### 5. Foreground Service Is Practically Required

overlay を安定して継続表示するため、foreground service の利用が実務上必要。

## Current Design Decision

- 対象 OS は Android 12 以上
- touch pass-through を優先するため Passive Annotation では alpha `0.78`
- `Hide` では compact な `Tools` チップへ移行する
- セッション終了時に描画を完全破棄する

## Feasibility Decision By Requirement

### Fully Feasible

- フローティングボタン表示
- 全画面描画
- ツール操作
- セッション終了時の全消去

### Feasible With Product Constraint

- 描画を残したまま下位アプリへ戻ること
  - alpha 制約あり
  - touch pass-through は一部 UX 制限あり

### Not Guaranteed Across All Apps

- 金融系やセキュア画面上での常時表示
- OEM 差分をまたぐ完全一致挙動

## Publication Risk

- `SYSTEM_ALERT_WINDOW` と foreground service は審査説明が重要
- Play Console では権限用途、foreground service 用途、動画説明の整合が必要
- 公開前にプライバシーポリシーとストア文言を揃える必要がある

## Final Recommendation

- V1 は現在の構成で公開可能
- ただし overlay 制約は仕様として明記する
- 実機差分確認と審査説明を優先する
