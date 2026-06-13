# 2026 中国官方节假日数据

用于 `D:\MyAndroidApp` 日历页的只读官方节假日数据。

文件：

- `CN_HOLIDAYS_2026.json`：推荐给 Claude/Android 代码直接读取或转换为 Kotlin seed data。
- `CN_HOLIDAYS_2026.csv`：人工检查用，也可导入表格。

来源：

- 标题：国务院办公厅关于2026年部分节假日安排的通知
- 发文字号：国办发明电〔2025〕7号
- 发布日期：2025-11-04
- 官方链接：https://www.gov.cn/zhengce/zhengceku/202511/content_7047091.htm

字段说明：

- `date`：ISO 日期。
- `weekday`：英文星期，供校验。
- `type`：
  - `holiday`：放假日。
  - `adjusted_workday`：调休上班日。
- `holiday_id`：节日稳定 key，适合代码判断。
- `name`：完整名称。
- `short_name`：月历格可显示的短名称。
- `display_label`：建议用于小标记，`休` 或 `班`。
- `description`：点击日期后的只读说明。
- `readonly`：官方数据不可由用户编辑。

显示建议：

1. 月历格仍以训练部位为主视觉。
2. 节假日只用小字、`休/班` 或淡色标记辅助表达。
3. 点击日期后，详情顺序为：官方节假日只读信息 → 用户个人备注 → 当天训练摘要。
4. 个人生日、纪念日等不要写入本文件，应存入用户专属 calendar note 表。

注意：

- 本数据只覆盖全国统一官方安排，不包含地方性节假日。
- 后续年份应等待官方通知后再更新，不自动采信非官方网页。
