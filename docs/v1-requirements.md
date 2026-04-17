# V1 Requirements

## Product Goal

見ている Android 画面の上に、そのまま線や注釈を書けること。

## Release Model

- V1 は無料版のみ
- V1 では Google Play Billing を入れない
- premium 候補は将来の需要確認後に判断する

## Free Version Definition

無料版は、画面に描けるというコア価値を十分に体験できる構成とする。

### Included In First Release

- フローティングボタンによる開始
- 全画面キャンバスでの描画
- ペン / 消しゴム / アンドゥ / リドゥ / 全消し
- 基本色: 黒 / 白 / 赤 / 青 / 黄
- 太さ、不透明度、線種の変更
- `Hide` による compact `Tools` 表示

### Excluded From First Release

- 描画保存
- 書き出し
- 共有
- Billing
- premium 購入導線

## Current Coverage Snapshot

| Requirement | Status | Notes |
| --- | --- | --- |
| FR-01 Overlay Launcher | Implemented | フローティングボタンを表示して開始できる |
| FR-02 Enter Drawing Mode | Implemented | 全画面キャンバスとツールパネルを表示する |
| FR-03 Drawing Tools | Implemented | ペン、消しゴム、アンドゥ、リドゥ、全消し、色、太さ、不透明度、線種に対応 |
| FR-04 Exit Drawing Mode | Implemented with constraint | `Hide` により passive annotation へ移行し、alpha `0.78` を固定 |
| FR-05 End Overlay Session | Implemented | セッション終了時に window と描画を破棄 |
| FR-06 Settings and State | Partially implemented | 実機差分と OEM 検証は継続中 |

## Functional Requirements

### FR-01 Overlay Launcher

- アプリ起動後にフローティングボタンを表示できる
- ボタンは画面上でドラッグ移動できる

### FR-02 Enter Drawing Mode

- フローティングボタンから描画モードに入れる
- 全画面キャンバスを表示する

### FR-03 Drawing Tools

- ペン
- 消しゴム
- アンドゥ
- リドゥ
- 全消し
- 色変更
- 太さ変更
- 不透明度変更
- 線種変更

### FR-04 Exit Drawing Mode

- `Hide` で描画を残したまま下位アプリへ戻れる
- 戻ったあとも compact な `Tools` チップから再開できる
- Passive Annotation 用 overlay alpha は `0.78`

### FR-05 End Overlay Session

- ユーザーが明示的に終了できる
- 終了時に描画データを消去する

## Premium Candidate List

- 追加カラーパレット
- 追加ペン種類
- 描画の保存と復元
- スクリーンショット保存
- 共有機能

## Guardrail

- 無料版でも十分使えること
- premium は無料版を不便にするためではなく、価値を拡張するために使うこと
