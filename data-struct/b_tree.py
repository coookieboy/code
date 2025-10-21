class BTreeNode:
    def __init__(self, t, leaf=False):
        """
        t: 最小度（min degree）。每个非根节点至少有 t-1 个 key，最多有 2*t-1 个 key。
        leaf: 是否为叶子节点
        """
        self.t = t
        self.leaf = leaf
        self.keys = []       # 存储键（有序）
        self.children = []   # 子节点列表，长度 = len(keys)+1 当不是叶子时

    def traverse(self):
        """中序遍历，返回 keys 的排序列表（所有子树合并）"""
        res = []
        for i, k in enumerate(self.keys):
            if not self.leaf:
                res.extend(self.children[i].traverse())
            res.append(k)
        if not self.leaf:
            res.extend(self.children[len(self.keys)].traverse())
        return res

    def search(self, key):
        """
        在子树中查找 key，返回 (node, index) 或 None
        """
        # 找到第一个 >= key 的位置 i
        i = 0
        while i < len(self.keys) and key > self.keys[i]:
            i += 1

        if i < len(self.keys) and self.keys[i] == key:
            return (self, i)

        if self.leaf:
            return None
        else:
            return self.children[i].search(key)

    def split_child(self, i):
        """
        假设 self.children[i] 已满（2*t - 1 个 key），将其分裂。
        在 self 中插入中间键，并在 children 中插入新节点。
        """
        t = self.t
        y = self.children[i]              # 被分裂的节点
        z = BTreeNode(t, leaf=y.leaf)     # 新节点 z

        # z 取得 y 的后 t-1 个 key
        z.keys = y.keys[t:]               # 从 index t 到 end (总共 t-1 个)
        # y 保留前 t-1 个 key（索引 0..t-2）
        mid_key = y.keys[t-1]             # 中间键，将上移到父节点
        y.keys = y.keys[:t-1]

        # 如果不是叶子，还要移动 children 指针
        if not y.leaf:
            z.children = y.children[t:]   # y.children 中后 t 个指针给 z
            y.children = y.children[:t]  # y 只保留前 t 个指针

        # 在父节点 self 中插入 z 与 mid_key
        self.children.insert(i+1, z)
        self.keys.insert(i, mid_key)

    def insert_non_full(self, key):
        """
        在非满节点中插入 key（保证 node.keys 长度 <= 2*t-2 前调用）
        """
        i = len(self.keys) - 1

        if self.leaf:
            # 插入到 keys 的正确位置（保持有序）
            self.keys.append(None)
            while i >= 0 and key < self.keys[i]:
                self.keys[i+1] = self.keys[i]
                i -= 1
            self.keys[i+1] = key
        else:
            # 找到将要下探到的子节点索引 i+1
            while i >= 0 and key < self.keys[i]:
                i -= 1
            i += 1
            # 如果目标子节点已满，先分裂
            if len(self.children[i].keys) == 2*self.t - 1:
                self.split_child(i)
                # 分裂后，可能需要选择右侧子节点
                if key > self.keys[i]:
                    i += 1
            self.children[i].insert_non_full(key)


class BTree:
    def __init__(self, t):
        """
        t: 最小度（min degree），t >= 2
        """
        if t < 2:
            raise ValueError("t must be >= 2")
        self.t = t
        self.root = BTreeNode(t, leaf=True)

    def traverse(self):
        return self.root.traverse()

    def search(self, key):
        return self.root.search(key)

    def insert(self, key):
        """
        插入 key 到 B 树（允许重复键吗？本实现允许重复键，会把重复值当作普通值插入到适当位置）
        """
        r = self.root
        # 根已满时，需要分裂并增加树高
        if len(r.keys) == 2*self.t - 1:
            s = BTreeNode(self.t, leaf=False)
            s.children.append(r)
            s.split_child(0)
            # 新根 s 成为树根
            self.root = s
            # 决定往哪一个子节点继续插入
            i = 0
            if key > s.keys[0]:
                i = 1
            s.children[i].insert_non_full(key)
        else:
            r.insert_non_full(key)

    def __str__(self):
        """简单字符串表示（层次打印）"""
        lines = []
        this_level = [self.root]
        while this_level:
            next_level = []
            level_keys = []
            for node in this_level:
                level_keys.append("[" + ",".join(str(k)
                                  for k in node.keys) + "]")
                if not node.leaf:
                    next_level.extend(node.children)
            lines.append(" ".join(level_keys))
            this_level = next_level
        return "\n".join(lines)


if __name__ == "__main__":
    # 最小度 t = 2 (每个节点最多 3 个 key)，这是一个小的 B-tree
    b = BTree(t=2)
    values = [10, 20, 5, 6, 12, 30, 7, 17, 18, 20, 50, 100, 3]
    for v in values:
        b.insert(v)

    print("树的层次表示：")
    print(b)                # 分层显示每个节点的 keys
    print("中序遍历（有序）：", b.traverse())
    # 查找
    found = b.search(6)
    print("搜索 6 ->", "found" if found else "not found")
    found = b.search(15)
    print("搜索 15 ->", "found" if found else "not found")
