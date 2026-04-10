# 2026-04-03 Release Prep Handoff

## 概要

2026-04-03 時点で、`overlay-pen` はクローズドテスト提出前の公開準備フェーズに入っている。  
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

- `store-assets/google-play/icon-512.png`
- `store-assets/google-play/feature-graphic-1024x500.png`
- 再生成スクリプト:
  - `scripts/export-store-icon.ps1`
  - `scripts/export-feature-graphic.ps1`

## プライバシーポリシー関連

- `docs/privacy-policy.md`
- `docs/privacy-policy.en.md`
- `docs/index.html`
- `docs/privacy-policy/index.html`
- `docs/privacy-policy/en/index.html`
- `docs/github-pages-setup.md`

## 署名と AAB 出力

- 出力物:
  - `app/build/outputs/bundle/release/app-release.aab`
- バージョン:
  - `versionCode = 2`
  - `versionName = 0.1.0`
- `app/build.gradle.kts` には `keystore.properties` がある場合だけ release signing する設定を追加済み
- `.gitignore` には `keystore.properties`、`*.jks`、`*.keystore` を追加済み

### ローカル専用ファイル

- `keystore/upload-keystore.jks`
- `keystore.properties`

これらは git 管理外。別 PC では安全な方法で復元する必要がある。

## Foreground Service 審査メモ

- 使用権限:
  - `FOREGROUND_SERVICE`
  - `FOREGROUND_SERVICE_SPECIAL_USE`
- 説明方針:
  - ユーザーが `オーバーレイ開始` を押した直後に描画 UI を即時表示する
  - 描画中、待機中、非表示中もユーザーが認識できるタスクを継続する
  - 一時停止や再起動が入るとオーバーレイ消失や描画セッション中断が起きる

## 直近で残っている作業

- Play Console の入力欄を最終確定
- クローズドテスト配布
- Foreground Service 審査用動画の撮影
- プライバシーポリシー公開用 public repo の整備
- upload key 保管方法の改善

## 補足

作業ツリーには `privacy-policy-public/` という未追跡ディレクトリが存在するが、現時点では本リポジトリ管理対象には含めていない。
