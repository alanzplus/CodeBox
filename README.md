Code Sandbox
--
This repo servers as a collection of implemetions of data structure and algorithms

## Java Based Implementation

### FastLFU
A constant time LFU implmentation based on paper [An O(1) algorithm for implementing the LFU cache eviction scheme](http://dhruvbird.com/lfu.pdf).

### Iterative Transformer
[IterativeTransformer](https://github.com/alanzplus/Sandbox/blob/master/java/algorithm-tool-box/src/main/java/org/zlambda/sandbox/algtoolbox/IterativeTransformer.java) is a really tiny and simple framework (40 lines of code) for writing iterative program in a recursive like fashion.

For example, given the following binary tree data structure

```java
/**
 *               1
 *            /     \
 *           2       3
 *                 /   \
 *                4     5
 */
private static TreeNode root = new TreeNode(1);
static {
    root.left = new TreeNode(2);
    root.right = new TreeNode(3);
    root.right.left = new TreeNode(4);
    root.right.right = new TreeNode(5);
}

private static class TreeNode {
    private TreeNode(int val) {
        this.val = val;
    }
    private TreeNode left;
    private TreeNode right;
    private final int val;
}
```

We can write typical recursive traversal as

```java
/**
 * The following recursive traversal is not the most elegant one, since we can simplify two functions into one like
 *
 * List<Integer> traverse(TreeNode node) {
 *      List<Integer> ret = new ArrayList<>();
 *      if (null == node) {
 *          return ret;
 *      }
 *      ret.add(node.val);
 *      ret.addAll(traverse(node.left));
 *      ret.addAll(traverse(node.right));
 * }
 * 
 * But writting as the following is just for comparing with one written using the "RecursiveTransformer"
 */
private List<Integer> traverse(TreeNode node) {
    List<Integer> ret = new ArrayList<>();
    preorderRecursive(node, ret);
    return ret;
}

private preorderRecursive(TreeNode node, List<Integer> ret) {
    if (null == node) {
        return ret;
    }
    ret.add(node.val);
    preorderRecursive(node.left, ret);
    preorderRecursive(node.right, ret);
}
```

Then by using the `IterativeTransformer` we can change the above implementation into iterative but writing in a recursive like way

```java
private List<Integer> traverse(TreeNode node) {
    List<Integer> ret = new ArrayList<>();
    IterativeTransformer transformer = new IterativeTransformer();
    transformer.recursiveCall(() -> new Preorder(root, ret, transformer));
    transformer.evaluate();
    return ret;
}

private static class Preorder {
    private Preorder(TreeNode node, List<Integer> ret, IterativeTransformer transformer) {
        if (null == node) {
            return;
        }
        transformer.wrap(() -> ret.add(node.val));
        transformer.recursiveCall(() -> new Preorder(node.left, ret, transformer));
        transformer.recursiveCall(() -> new Preorder(node.right, ret, transformer));
    }
}
```

Parameters and return value are passing using the user define constructor interface. For example, given the original recursive function like

```java
RetType recursiveFun(ArgType1 arg1, ArgType2 arg2, ..., ArgTypeN argN) {
    ...
}
```

then it is recommended to define the recursive object constructor interface like

```java
class RecursiveObject {
    RecursiveObject(ArgType1 arg1, ArgType2 arg2, ..., ArgTypeN argN, Holder<RetType> ret) {
    }
}
```

Another example is writing a Binary Tree Clone procedure

Typical recursive one

```java
TreeNode clone(TreeNode root) {
    if (null == root) {
        return null;
    }
    TreeNode newNode = new TreeNode(node.val);
    newNode.left = clone(root.left);
    newNode.right = clone(root.right);
    return newNode;
}
```

By using `IterativeTransformer`

```java
TreeNode clone(TreeNode root) {
    Holder<TreeNode> ret = new Holder<>();
    IterativeTransformer transformer = new IterativeTransformer();
    transformer.recursiveCall(() -> new Cloner(root, ret, transformer));
    transformer.evaluate();
    return ret.get();
}

class Cloner {
    private Cloner(TreeNode node, Holder<TreeNode> ret, IterativeTransformer transformer) {
        if (null == node) {
            return;
        }
        transformer.wrap(() -> ret.set(new TreeNode(node.val)));
        {
            Holder<TreeNode> rret = new Holder<>();
            transformer.recursiveCall(() -> new Cloner(node.left, rret, transformer));
            transformer.wrap(() -> ret.get().left = rret.get());
        }
        {
            Holder<TreeNode> rret = new Holder<>();
            transformer.recursiveCall(() -> new Cloner(node.right, rret, transformer));
            transformer.wrap(() -> ret.get().right = rret.get());
        }
    }
}
```

