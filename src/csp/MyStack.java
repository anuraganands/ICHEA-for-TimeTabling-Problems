/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.util.Collection;
import java.util.Stack;
import javax.naming.SizeLimitExceededException;

/**
 *
 * @author sharma_au
 */
public class MyStack<T>{
    private Stack<T> s;
    private int size;
    /**
     * Constant size stack
     * @param size 
     */
    public MyStack(int size){
        s = new Stack<T>();
        this.size = size; 
    }

    /**
     * unlimited sized stack.
     */
    public MyStack() {
        s = new Stack<T>();
        size = -1;
    }
    
    /**
     * Same as {@link Stack#pop() }.
     * @return 
     */
    public T pop(){
        return s.pop();        
    }
    
    /**
     * same as {@link Stack#peek() }.
     * @return 
     */
    public T peek(){
        return s.peek();
    }
    
    public T push(T item) {
        if(size == -1)//size not specified
            return s.push(item);
        else
            throw new UnsupportedOperationException("Use T tryPush(T item) method");
    }

    
    public T tryPush(T item) throws SizeLimitExceededException{
        if(s.size() >= this.size){
            throw new SizeLimitExceededException("Stack is full");            
        }else{
            return s.push(item); 
        }
    }
    
    /**
     * applicable for fixed sized stack only
     * @param item
     * @throws SizeLimitExceededException 
     */
    public void forcePushByDequeue(T item) throws SizeLimitExceededException{
        if(capacity() == -1){
            throw new UnsupportedOperationException("Use T tryPush(T item) method");
        }
        if(this.size()<this.capacity()){                        
           this.tryPush(item); 
        } else {
            this.forceDequeue();
            this.tryPush(item);//most likely it will not throw again
        }
    }
    
    
    public T forceDequeue(){
        return s.remove(0);
    }
    
    /**
     * Forcibly get an element from anywhere in the stack. It does not removes
     * the element.
     * @param i
     * @return 
     */
    public T forceGet(int i){
        return s.get(i);
    }
    
    /**
     * Same as {@link Stack#size() }.
     * @return 
     */
    public final int size(){
        return s.size();
    }
    
    public final boolean isEmpty(){
        return s.isEmpty();
    }
    
    public final boolean isFull(){
        if(s.size() == this.size){
            return true;
        }
        return false;
    }
    
    public void clear(){
        s.clear();
    }
    
    /**
     * The maximum elements that this stack can hold. If the returned value is
     * negative then size is <B>unlimited</B>.
     * @return 
     */
    public final int capacity(){
        return this.size;
    }

    @Override
    public String toString() {
        return s.toString();
    }
    
    
}
