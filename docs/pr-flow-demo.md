# PR 示例流程（面向社区）

下面是完整的社区贡献 PR 示例流程，目标分支为 `main`。

## 方式 A：命令行流程

### 1. Fork 或克隆仓库
开发者先 Fork 仓库到自己账号，或直接克隆：

```bash
git clone https://github.com/<your-username>/field-translate-starter-open.git
cd field-translate-starter-open
```

可选：添加上游仓库（保持与主仓库同步）

```bash
git remote add upstream https://github.com/tom-9527/field-translate-starter-open.git
```

### 2. 创建 feature 分支并提交修改

```bash
git checkout -b feat/add-dict-handler
# 修改代码...
git add .
git commit -m "feat: add dict translate handler"
```

### 3. Push 到远端 feature 分支

```bash
git push -u origin feat/add-dict-handler
```

### 4. 发 PR 到 main

```bash
gh pr create \
  --repo tom-9527/field-translate-starter-open \
  --base main \
  --head <your-username>:feat/add-dict-handler \
  --title "feat: add dict translate handler" \
  --body "Add handler for dictionary-based translation."
```

### 5. 审核人（维护者）合并 PR
维护者在 GitHub 上 Review，通过后合并。

```bash
gh pr merge <PR_NUMBER> --repo tom-9527/field-translate-starter-open --merge
```

## 方式 B：流程图（简化）

```
Fork/Clone
    |
    v
Create Feature Branch
    |
    v
Commit Changes
    |
    v
Push Feature Branch
    |
    v
Open PR -> main
    |
    v
Maintainer Review
    |
    v
Merge PR
```
