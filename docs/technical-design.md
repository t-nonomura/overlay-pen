# Technical Design

## Chosen Direction

- Platform: Android native
- Language: Kotlin
- Minimum OS: Android 12
- Activity onboarding: Jetpack Compose
- Overlay runtime: `WindowManager` + custom `View`

## Current Implementation Status

- `MainActivity` で権限案内、開始 / 停止導線、状態説明を提供
- `OverlayService` で foreground service と複数 overlay window を管理
- `DrawingSessionStore` でブラシ設定、ストローク、Undo / Redo を管理
- `OverlayCanvasView` で bitmap ベースの描画を実装
- `ToolPaletteView` でツール操作、色、太さ、透明度、線種を提供
- `OverlayPositioning` で bubble / panel / chip の配置制御を実装

## Runtime Components

### `MainActivity`

- 権限案内
- 設定画面への導線
- overlay の開始 / 停止

### `OverlayService`

- foreground service の常駐
- bubble、canvas、palette、chip の lifecycle 管理
- `Hide`、`Resume`、`Clear`、`Stop` の遷移管理

### `DrawingSessionStore`

- 現在のブラシ設定
- ストローク一覧
- Undo / Redo
- セッション単位の描画状態

### `OverlayCanvasView`

- ストローク描画
- bitmap キャッシュ
- `BlendMode.CLEAR` による消しゴム

### `ToolPaletteView`

- ペン
- 消しゴム
- アンドゥ
- リドゥ
- 全消し
- 線種切り替え
- 太さ、透明度、色の調整
- `Hide` と `Close`

## Overlay States

1. Idle
   - overlay 非表示
2. Drawing
   - 全画面 canvas と palette を表示
   - 下位アプリへの touch は block
3. Passive Annotation
   - 描画だけを残す
   - compact な `Tools` チップを表示
   - overlay alpha は `0.78`
4. Stopped
   - window とセッションを終了

## Implemented Behavior Notes

- `Hide` では interactive canvas を閉じ、描画だけを残す
- `Clear` はセッションを維持したまま描画だけを消す
- セッション終了時は描画を完全に破棄する
- bubble と chip の位置はドラッグ位置を保持する

## Tradeoffs Accepted For V1

- 保存や共有は V1 では扱わない
- Passive Annotation では下位アプリ操作を優先して alpha `0.78` を採用する
- 一部アプリで overlay が隠される可能性は既知制約として扱う

## Validation Status

- `DrawingSessionStore` の描画状態と Redo は unit test 済み
- `OverlayPositioning` の clamp は unit test 済み
- 実機での OEM 差分確認は継続中
