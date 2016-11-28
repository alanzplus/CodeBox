package org.zlambda.codebox.algtoolbox;

import java.util.ArrayList;
import java.util.List;

public class IterativeTransformerTest {
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


    public static void main(String[] args) {
        // pre-order
        {
            List<Integer> ret = new ArrayList<>();
            IterativeTransformer transformer = new IterativeTransformer();
            transformer.recursiveCall(() -> new Preorder(root, ret, transformer));
            transformer.evaluate();
            System.out.println(ret);
        }
        // inorder
        {
            List<Integer> ret = new ArrayList<>();
            IterativeTransformer transformer = new IterativeTransformer();
            transformer.recursiveCall(() -> new Inorder(root, ret, transformer));
            transformer.evaluate();
            System.out.println(ret);
        }
        // post-order
        {
            List<Integer> ret = new ArrayList<>();
            IterativeTransformer transformer = new IterativeTransformer();
            transformer.recursiveCall(() -> new Postorder(root, ret, transformer));
            transformer.evaluate();
            System.out.println(ret);
        }
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

    private static class Inorder {
        private Inorder(TreeNode node, List<Integer> ret, IterativeTransformer transformer) {
            if (null == node) {
                return;
            }
            transformer.recursiveCall(() -> new Inorder(node.left, ret, transformer));
            transformer.wrap(() -> ret.add(node.val));
            transformer.recursiveCall(() -> new Inorder(node.right, ret, transformer));
        }
    }

    private static class Postorder {
        private Postorder(TreeNode node, List<Integer> ret, IterativeTransformer transformer) {
            if (null == node) {
                return;
            }
            transformer.recursiveCall(() -> new Postorder(node.left, ret, transformer));
            transformer.recursiveCall(() -> new Postorder(node.right, ret, transformer));
            transformer.wrap(() -> ret.add(node.val));
        }
    }
}