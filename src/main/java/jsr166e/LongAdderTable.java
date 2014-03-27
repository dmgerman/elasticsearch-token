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
name|jsr166e
operator|.
name|LongAdder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

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
comment|/**  * A keyed table of adders, that may be useful in computing frequency  * counts and histograms, or may be used as a form of multiset.  A  * {@link LongAdder} is associated with each key. Keys are added to  * the table implicitly upon any attempt to update, or may be added  * explicitly using method {@link #install}.  *  *<p><em>jsr166e note: This class is targeted to be placed in  * java.util.concurrent.atomic.</em>  *  * @since 1.8  * @author Doug Lea  */
end_comment

begin_class
DECL|class|LongAdderTable
specifier|public
class|class
name|LongAdderTable
parameter_list|<
name|K
parameter_list|>
implements|implements
name|Serializable
block|{
comment|/** Relies on default serialization */
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|7249369246863182397L
decl_stmt|;
comment|/** The underlying map */
DECL|field|map
specifier|private
specifier|final
name|ConcurrentHashMapV8
argument_list|<
name|K
argument_list|,
name|LongAdder
argument_list|>
name|map
decl_stmt|;
DECL|class|CreateAdder
specifier|static
specifier|final
class|class
name|CreateAdder
implements|implements
name|ConcurrentHashMapV8
operator|.
name|Fun
argument_list|<
name|Object
argument_list|,
name|LongAdder
argument_list|>
block|{
DECL|method|apply
specifier|public
name|LongAdder
name|apply
parameter_list|(
name|Object
name|unused
parameter_list|)
block|{
return|return
operator|new
name|LongAdder
argument_list|()
return|;
block|}
block|}
DECL|field|createAdder
specifier|private
specifier|static
specifier|final
name|CreateAdder
name|createAdder
init|=
operator|new
name|CreateAdder
argument_list|()
decl_stmt|;
comment|/**      * Creates a new empty table.      */
DECL|method|LongAdderTable
specifier|public
name|LongAdderTable
parameter_list|()
block|{
name|map
operator|=
operator|new
name|ConcurrentHashMapV8
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**      * If the given key does not already exist in the table, inserts      * the key with initial sum of zero; in either case returning the      * adder associated with this key.      *      * @param key the key      * @return the adder associated with the key      */
DECL|method|install
specifier|public
name|LongAdder
name|install
parameter_list|(
name|K
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|computeIfAbsent
argument_list|(
name|key
argument_list|,
name|createAdder
argument_list|)
return|;
block|}
comment|/**      * Adds the given value to the sum associated with the given      * key.  If the key does not already exist in the table, it is      * inserted.      *      * @param key the key      * @param x the value to add      */
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|K
name|key
parameter_list|,
name|long
name|x
parameter_list|)
block|{
name|map
operator|.
name|computeIfAbsent
argument_list|(
name|key
argument_list|,
name|createAdder
argument_list|)
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
comment|/**      * Increments the sum associated with the given key.  If the key      * does not already exist in the table, it is inserted.      *      * @param key the key      */
DECL|method|increment
specifier|public
name|void
name|increment
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|add
argument_list|(
name|key
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
block|}
comment|/**      * Decrements the sum associated with the given key.  If the key      * does not already exist in the table, it is inserted.      *      * @param key the key      */
DECL|method|decrement
specifier|public
name|void
name|decrement
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|add
argument_list|(
name|key
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the sum associated with the given key, or zero if the      * key does not currently exist in the table.      *      * @param key the key      * @return the sum associated with the key, or zero if the key is      * not in the table      */
DECL|method|sum
specifier|public
name|long
name|sum
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|LongAdder
name|a
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|a
operator|==
literal|null
condition|?
literal|0L
else|:
name|a
operator|.
name|sum
argument_list|()
return|;
block|}
comment|/**      * Resets the sum associated with the given key to zero if the key      * exists in the table.  This method does<em>NOT</em> add or      * remove the key from the table (see {@link #remove}).      *      * @param key the key      */
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|LongAdder
name|a
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|a
operator|!=
literal|null
condition|)
name|a
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**      * Resets the sum associated with the given key to zero if the key      * exists in the table.  This method does<em>NOT</em> add or      * remove the key from the table (see {@link #remove}).      *      * @param key the key      * @return the previous sum, or zero if the key is not      * in the table      */
DECL|method|sumThenReset
specifier|public
name|long
name|sumThenReset
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|LongAdder
name|a
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|a
operator|==
literal|null
condition|?
literal|0L
else|:
name|a
operator|.
name|sumThenReset
argument_list|()
return|;
block|}
comment|/**      * Returns the sum totalled across all keys.      *      * @return the sum totalled across all keys      */
DECL|method|sumAll
specifier|public
name|long
name|sumAll
parameter_list|()
block|{
name|long
name|sum
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|LongAdder
name|a
range|:
name|map
operator|.
name|values
argument_list|()
control|)
name|sum
operator|+=
name|a
operator|.
name|sum
argument_list|()
expr_stmt|;
return|return
name|sum
return|;
block|}
comment|/**      * Resets the sum associated with each key to zero.      */
DECL|method|resetAll
specifier|public
name|void
name|resetAll
parameter_list|()
block|{
for|for
control|(
name|LongAdder
name|a
range|:
name|map
operator|.
name|values
argument_list|()
control|)
name|a
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**      * Totals, then resets, the sums associated with all keys.      *      * @return the sum totalled across all keys      */
DECL|method|sumThenResetAll
specifier|public
name|long
name|sumThenResetAll
parameter_list|()
block|{
name|long
name|sum
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|LongAdder
name|a
range|:
name|map
operator|.
name|values
argument_list|()
control|)
name|sum
operator|+=
name|a
operator|.
name|sumThenReset
argument_list|()
expr_stmt|;
return|return
name|sum
return|;
block|}
comment|/**      * Removes the given key from the table.      *      * @param key the key      */
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|/**      * Removes all keys from the table.      */
DECL|method|removeAll
specifier|public
name|void
name|removeAll
parameter_list|()
block|{
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the current set of keys.      *      * @return the current set of keys      */
DECL|method|keySet
specifier|public
name|Set
argument_list|<
name|K
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|map
operator|.
name|keySet
argument_list|()
return|;
block|}
comment|/**      * Returns the current set of key-value mappings.      *      * @return the current set of key-value mappings      */
DECL|method|entrySet
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|LongAdder
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|map
operator|.
name|entrySet
argument_list|()
return|;
block|}
block|}
end_class

end_unit

