begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Written by Doug Lea with assistance from members of JCP JSR-166  * Expert Group and released to the public domain, as explained at  * http://creativecommons.org/publicdomain/zero/1.0/  */
end_comment

begin_package
DECL|package|jsr166e
package|package
name|jsr166e
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * One or more variables that together maintain a running {@code double}  * maximum with initial value {@code Double.NEGATIVE_INFINITY}.  When  * updates (method {@link #update}) are contended across threads, the  * set of variables may grow dynamically to reduce contention.  Method  * {@link #max} (or, equivalently, {@link #doubleValue}) returns the  * current maximum across the variables maintaining updates.  *  *<p>This class extends {@link Number}, but does<em>not</em> define  * methods such as {@code equals}, {@code hashCode} and {@code  * compareTo} because instances are expected to be mutated, and so are  * not useful as collection keys.  *  *<p><em>jsr166e note: This class is targeted to be placed in  * java.util.concurrent.atomic.</em>  *  * @since 1.8  * @author Doug Lea  */
end_comment

begin_class
DECL|class|DoubleMaxUpdater
specifier|public
class|class
name|DoubleMaxUpdater
extends|extends
name|Striped64
implements|implements
name|Serializable
block|{
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|7249069246863182397L
decl_stmt|;
comment|/**      * Long representation of negative infinity. See class Double      * internal documentation for explanation.      */
DECL|field|MIN_AS_LONG
specifier|private
specifier|static
specifier|final
name|long
name|MIN_AS_LONG
init|=
literal|0xfff0000000000000L
decl_stmt|;
comment|/**      * Update function. See class DoubleAdder for rationale      * for using conversions from/to long.      */
DECL|method|fn
specifier|final
name|long
name|fn
parameter_list|(
name|long
name|v
parameter_list|,
name|long
name|x
parameter_list|)
block|{
return|return
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|v
argument_list|)
operator|>
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|x
argument_list|)
condition|?
name|v
else|:
name|x
return|;
block|}
comment|/**      * Creates a new instance with initial value of {@code      * Double.NEGATIVE_INFINITY}.      */
DECL|method|DoubleMaxUpdater
specifier|public
name|DoubleMaxUpdater
parameter_list|()
block|{
name|base
operator|=
name|MIN_AS_LONG
expr_stmt|;
block|}
comment|/**      * Updates the maximum to be at least the given value.      *      * @param x the value to update      */
DECL|method|update
specifier|public
name|void
name|update
parameter_list|(
name|double
name|x
parameter_list|)
block|{
name|long
name|lx
init|=
name|Double
operator|.
name|doubleToRawLongBits
argument_list|(
name|x
argument_list|)
decl_stmt|;
name|Cell
index|[]
name|as
decl_stmt|;
name|long
name|b
decl_stmt|,
name|v
decl_stmt|;
name|HashCode
name|hc
decl_stmt|;
name|Cell
name|a
decl_stmt|;
name|int
name|n
decl_stmt|;
if|if
condition|(
operator|(
name|as
operator|=
name|cells
operator|)
operator|!=
literal|null
operator|||
operator|(
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|b
operator|=
name|base
argument_list|)
operator|<
name|x
operator|&&
operator|!
name|casBase
argument_list|(
name|b
argument_list|,
name|lx
argument_list|)
operator|)
condition|)
block|{
name|boolean
name|uncontended
init|=
literal|true
decl_stmt|;
name|int
name|h
init|=
operator|(
name|hc
operator|=
name|threadHashCode
operator|.
name|get
argument_list|()
operator|)
operator|.
name|code
decl_stmt|;
if|if
condition|(
name|as
operator|==
literal|null
operator|||
operator|(
name|n
operator|=
name|as
operator|.
name|length
operator|)
operator|<
literal|1
operator|||
operator|(
name|a
operator|=
name|as
index|[
operator|(
name|n
operator|-
literal|1
operator|)
operator|&
name|h
index|]
operator|)
operator|==
literal|null
operator|||
operator|(
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|v
operator|=
name|a
operator|.
name|value
argument_list|)
operator|<
name|x
operator|&&
operator|!
operator|(
name|uncontended
operator|=
name|a
operator|.
name|cas
argument_list|(
name|v
argument_list|,
name|lx
argument_list|)
operator|)
operator|)
condition|)
name|retryUpdate
argument_list|(
name|lx
argument_list|,
name|hc
argument_list|,
name|uncontended
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Returns the current maximum.  The returned value is      *<em>NOT</em> an atomic snapshot; invocation in the absence of      * concurrent updates returns an accurate result, but concurrent      * updates that occur while the value is being calculated might      * not be incorporated.      *      * @return the maximum      */
DECL|method|max
specifier|public
name|double
name|max
parameter_list|()
block|{
name|Cell
index|[]
name|as
init|=
name|cells
decl_stmt|;
name|double
name|max
init|=
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|base
argument_list|)
decl_stmt|;
if|if
condition|(
name|as
operator|!=
literal|null
condition|)
block|{
name|int
name|n
init|=
name|as
operator|.
name|length
decl_stmt|;
name|double
name|v
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|Cell
name|a
init|=
name|as
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|a
operator|!=
literal|null
operator|&&
operator|(
name|v
operator|=
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|a
operator|.
name|value
argument_list|)
operator|)
operator|>
name|max
condition|)
name|max
operator|=
name|v
expr_stmt|;
block|}
block|}
return|return
name|max
return|;
block|}
comment|/**      * Resets variables maintaining updates to {@code      * Double.NEGATIVE_INFINITY}.  This method may be a useful      * alternative to creating a new updater, but is only effective if      * there are no concurrent updates.  Because this method is      * intrinsically racy, it should only be used when it is known      * that no threads are concurrently updating.      */
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|internalReset
argument_list|(
name|MIN_AS_LONG
argument_list|)
expr_stmt|;
block|}
comment|/**      * Equivalent in effect to {@link #max} followed by {@link      * #reset}. This method may apply for example during quiescent      * points between multithreaded computations.  If there are      * updates concurrent with this method, the returned value is      *<em>not</em> guaranteed to be the final value occurring before      * the reset.      *      * @return the maximum      */
DECL|method|maxThenReset
specifier|public
name|double
name|maxThenReset
parameter_list|()
block|{
name|Cell
index|[]
name|as
init|=
name|cells
decl_stmt|;
name|double
name|max
init|=
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|base
argument_list|)
decl_stmt|;
name|base
operator|=
name|MIN_AS_LONG
expr_stmt|;
if|if
condition|(
name|as
operator|!=
literal|null
condition|)
block|{
name|int
name|n
init|=
name|as
operator|.
name|length
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|Cell
name|a
init|=
name|as
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|a
operator|!=
literal|null
condition|)
block|{
name|double
name|v
init|=
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|a
operator|.
name|value
argument_list|)
decl_stmt|;
name|a
operator|.
name|value
operator|=
name|MIN_AS_LONG
expr_stmt|;
if|if
condition|(
name|v
operator|>
name|max
condition|)
name|max
operator|=
name|v
expr_stmt|;
block|}
block|}
block|}
return|return
name|max
return|;
block|}
comment|/**      * Returns the String representation of the {@link #max}.      * @return the String representation of the {@link #max}      */
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Double
operator|.
name|toString
argument_list|(
name|max
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Equivalent to {@link #max}.      *      * @return the max      */
DECL|method|doubleValue
specifier|public
name|double
name|doubleValue
parameter_list|()
block|{
return|return
name|max
argument_list|()
return|;
block|}
comment|/**      * Returns the {@link #max} as a {@code long} after a      * narrowing primitive conversion.      */
DECL|method|longValue
specifier|public
name|long
name|longValue
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|max
argument_list|()
return|;
block|}
comment|/**      * Returns the {@link #max} as an {@code int} after a      * narrowing primitive conversion.      */
DECL|method|intValue
specifier|public
name|int
name|intValue
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|max
argument_list|()
return|;
block|}
comment|/**      * Returns the {@link #max} as a {@code float}      * after a narrowing primitive conversion.      */
DECL|method|floatValue
specifier|public
name|float
name|floatValue
parameter_list|()
block|{
return|return
operator|(
name|float
operator|)
name|max
argument_list|()
return|;
block|}
DECL|method|writeObject
specifier|private
name|void
name|writeObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
name|s
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
name|s
operator|.
name|defaultWriteObject
argument_list|()
expr_stmt|;
name|s
operator|.
name|writeDouble
argument_list|(
name|max
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|readObject
specifier|private
name|void
name|readObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
name|s
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|s
operator|.
name|defaultReadObject
argument_list|()
expr_stmt|;
name|busy
operator|=
literal|0
expr_stmt|;
name|cells
operator|=
literal|null
expr_stmt|;
name|base
operator|=
name|Double
operator|.
name|doubleToRawLongBits
argument_list|(
name|s
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

