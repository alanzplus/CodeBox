package org.zlambda.codebox.algtoolbox;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class IterativeTransformer {
    private final Stack<Frame> stack = new Stack<Frame>() {{ push(new Frame()); }};
    private boolean first = true;

    public void wrap(Thunk thunk) {
        stack.peek().thunks.add(thunk);
    }

    public void recursiveCall(Thunk thunk) {
        stack.peek().thunks.add(() -> {
            stack.push(new Frame());
            thunk.evaluate();
            /**
             * Same as :
             * stack.peek().thunks.add(() -> stack.pop());
             */
            stack.peek().thunks.add(stack::pop);
        });
        if (first) {
            stack.peek().thunks.add(stack::pop);
            first = false;
        }
    }

    public void evaluate() {
        while (!stack.isEmpty()) {
            stack.peek().thunks.poll().evaluate();
        }
    }

    public interface Thunk {
        void evaluate();
    }

    private class Frame {
        private final Queue<Thunk> thunks = new LinkedList<>();
    }
}
