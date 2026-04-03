# 2026-04-03 Release Prep Handoff

## 概要

2026-04-03 時点で、`overlay-pen` はクローズドテスト提出前のリリース準備フェーズに入っている。  
アプリ名、ストア素材、署名付き AAB 出力、プライバシーポリシー草案まで一通り揃っている。

## 現在のアプリ名と方向性

- アプリ名は `最前面ペン`
- コンセプトは「画面に重ねて描ける注釈ツール」
- 初回リリースは無料版の最小構成
- 課金 UI や Billing はまだ入れない
- 将来の premium 候補は、追加色、追加ペン種類、保存や共有など

## 実装上の主な状態

- オーバーレイ起動、描画、色変更、太さ変更、透明度変更が可能
- ペン、消しゴム、アンドゥ、リドゥ、全消しあり
- 線種は通常、蛍光、破線
- 無料版の基本色は `黒 / 白 / 赤 / 青 / 黄`
- ツールパネルはアイコン UI 化済み
- `Undo / Redo` は隣接配置に整理済み
- ツールパネル幅は `288dp`
- アプリ名変更に合わせてホーム画面、通知、ツールパネル文言も更新済み

## ストア掲載向けに用意したもの

### ストア用アイコン

- ファイル: `store-assets/google-play/icon-512.png`
- サイズ: `512x512`
- 再生成スクリプト: `scripts/export-store-icon.ps1`

### フィーチャーグラフィック

- ファイル: `store-assets/google-play/feature-graphic-1024x500.png`
- サイズ: `1024x500`
- 再生成スクリプト: `scripts/export-feature-graphic.ps1`

### ストア文言の方向性

- 短い説明案:
  - `画面に重ねて描ける、シンプルな注釈ツール`
- カテゴリ候補:
  - `ツール`
- タグ候補:
  - `ツール`
  - `仕事効率化`
  - `メモ`

## プライバシーポリシー関連

### 本体リポジトリ内にある草案

- `docs/privacy-policy.md`
- `docs/privacy-policy.en.md`
- `docs/index.html`
- `docs/privacy-policy/index.html`
- `docs/privacy-policy/en/index.html`
- `docs/github-pages-setup.md`

### 運用方針

- アプリ本体リポジトリ `overlay-pen` は private のまま運用
- プライバシーポリシー公開用リポジトリは別の public repo で管理する方針
- GitHub Pages で公開する想定
- 公開前に差し替える必要があるのは以下
  - 公開用メールアドレス
  - 実際の公開 URL

## 署名と AAB 出力

### 現在の出力物

- クローズドテスト提出用 AAB:
  - `app/build/outputs/bundle/release/app-release.aab`

### バージョン

- `versionCode = 2`
- `versionName = 0.1.0`

### 署名設定

- `app/build.gradle.kts` に、`keystore.properties` が存在する場合のみ release signing する設定を追加済み
- `.gitignore` に以下を追加済み
  - `keystore.properties`
  - `*.jks`
  - `*.keystore`

### ローカル専用ファイル

以下は git 管理外で、ローカルにのみ存在する想定。

- `keystore/upload-keystore.jks`
- `keystore.properties`

### 注意

- upload key の保管方法は今後改善予定
- 現時点では AAB 生成は可能だが、秘密情報は別 PC へは同期されない
- 別 PC で release build を行う場合は、keystore と `keystore.properties` を安全な方法で復元する必要がある

## Foreground Service 審査メモ

### 使用権限

- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_SPECIAL_USE`

### Play Console 向けの説明方針

- ユーザーが `オーバーレイ開始` を操作した直後に、他アプリ上へ描画 UI を即時表示するために使用
- 描画中、待機中、非表示中も、ユーザーが認識できる描画セッション状態を継続する必要がある
- 一時停止や再起動が入ると、オーバーレイ消失や描画セッション中断が起きるため不適切

### 動画の想定内容

- アプリ起動
- `オーバーレイ開始`
- 通知表示確認
- 他アプリ上でフローティング UI が表示されることを確認
- 実際に描画
- `隠す`
- 再表示
- 終了

## 別 PC でまず確認するとよいもの

1. `README.md`
2. `docs/PROJECT.md`
3. `docs/v1-requirements.md`
4. `docs/technical-design.md`
5. 本ログ `docs/logs/2026-04-03-release-prep-handoff.md`

## 直近で残っている作業

- Play Console の各入力欄を最終確定
- クローズドテスト配布
- Foreground Service 審査用動画の撮影
- プライバシーポリシー公開用 public repo の整備
- upload key 保管方法の改善

## 補足

作業ツリーには `privacy-policy-public/` という未追跡ディレクトリが存在するが、現時点では本リポジトリ管理対象には含めていない。
