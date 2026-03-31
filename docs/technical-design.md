# Technical Design

## Chosen Direction

- Platform: Android native
- Language: Kotlin
- Minimum OS: Android 12
- UI split:
  - Activity onboarding: Jetpack Compose
  - Overlay runtime: `WindowManager` + custom `View`

## Why This Architecture

- オーバーレイ描画は `WindowManager` と通常 `View` の方がタッチ制御と描画制御を明示しやすい
- Activity 側は Compose の方が権限説明やステータス表示を短く保守しやすい
- 描画を永続化しないため、ローカル DB やファイル層は V1 では不要

## Runtime Components

### `MainActivity`

- Overlay 権限状態の確認
- 権限設定画面への導線
- Overlay service の開始と停止

### `OverlayService`

- Foreground Service として常駐
- 通知の表示
- フローティングボタン、描画キャンバス、ツールパレットの lifecycle 管理

### `DrawingSessionStore`

- 現在のブラシ設定
- 描画ストローク一覧
- Undo
- リスナー通知

### `OverlayCanvasView`

- ストローク描画
- 入力中ストロークのプレビュー
- 消しゴムは `BlendMode.CLEAR` で実装
- 座標は正規化して保持し、画面サイズ変化に追従しやすくする
- 確定済みストロークは bitmap キャッシュへ描き込み、入力中だけを前面で再描画する

### `ToolPaletteView`

- ペン
- 消しゴム
- アンドゥ
- クリア
- 太さ
- 透明度
- 色
- ペン種別切り替え
- Keep と Close
- Move ハンドルによる再配置
- ストロークがないときは `Undo`、`Clear`、`Keep` を無効化
- `Hide` では interactive canvas を外し、passive overlay と compact な `Tools` チップへ切り替える

## Overlay States

1. Idle
   - フローティングボタンのみ
2. Drawing
   - 全画面キャンバスとツールパレットを表示
   - 下位アプリのタッチはブロック
3. Passive Annotation
   - 描画だけを残す
   - フローティングボタンを戻す
   - overlay window alpha は `0.78`
4. Stopped
   - すべての window と描画を破棄

## Tradeoffs Accepted For V1

- 描画保存はしない
- 一部アプリでは overlay 非表示の可能性がある
- Passive Annotation では描画自体も 78 パーセント相当の見え方になる
- bubble とパネル位置はセッション内保持のみで、アプリ再起動をまたいでは保持しない
- 画面端吸着は左右のみで、上下方向は安全マージン付きの clamp に留める

## Next Technical Steps

1. 実機で Pixel と Galaxy の overlay 挙動を確認
2. 通知からの再開導線を調整
3. 描画パネルの位置固定と衝突回避を改善
4. OEM ごとのバックグラウンド制限を検証
