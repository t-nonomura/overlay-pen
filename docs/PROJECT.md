# PROJECT

## Mission

他のアプリを開いたまま使える、Android 向けの画面注釈ツールを提供する。

## Current Phase

- 現在はクローズドテストと公開準備フェーズ
- 基本機能の実装は揃っており、主な焦点は実機検証、ストア審査対応、公開品質の調整
- 次の焦点は OEM 差分確認、審査説明、公開後の改善準備

## Team Operation

このプロジェクトは 5 ロール前提で判断する。

- Product Owner: リリース方針、V1 / 将来 premium の切り分け
- Android Lead: 実装、品質、将来拡張に耐える構造
- UX Designer: 操作性、用語、表示の分かりやすさ
- QA Lead: 実機検証、回帰確認、既知制約の整理
- Security and Policy Owner: Google Play、権限、ポリシー、審査説明

## Milestone Status

1. Discovery
   - 完了
2. Initial implementation
   - 完了
3. Technical validation
   - 進行中
4. Closed testing
   - 進行中
5. Public release
   - 準備中

## Release and Monetization Policy

- ファーストリリースは無料版のみで公開する
- V1 では Billing、購入 UI、未解放 premium 導線を入れない
- premium 候補は将来需要を確認したうえで導入判断する
- premium 候補の設計はしても、V1 ではユーザーに見せない

## Free Release Completion Criteria By Role

### Product Owner

- 公開価値のあるコア体験が整理されている
- V1 と将来 premium の境界が明確である
- ストア説明と公開範囲が確定している

### Android Lead

- 主なフローが Android 12 以上で安定動作する
- V1 に不要な Billing / entitlement 実装が入っていない
- 将来 premium 候補を切り出せる構造になっている

### UX Designer

- 初回起動から描画開始まで迷わない
- ツールの意味が直感的に分かる
- compact 表示時でも戻り方が分かる

### QA Lead

- 主な端末と OS で基本フロー確認が完了している
- 既知制約が明文化されている
- 回帰観点が揃っている

### Security and Policy Owner

- 権限、foreground service、ストア文言が整合している
- プライバシーポリシーと申告内容が揃っている
- 公開時の説明責任を果たせる状態である
