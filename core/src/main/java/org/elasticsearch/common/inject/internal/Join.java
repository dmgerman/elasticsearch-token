begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|CollectionUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Utility for joining pieces of text separated by a delimiter. It can handle  * iterators, collections, arrays, and varargs, and can append to any  * {@link Appendable} or just return a {@link String}. For example,  * {@code join(":", "a", "b", "c")} returns {@code "a:b:c"}.  *<p>  * All methods of this class throw {@link NullPointerException} when a value  * of {@code null} is supplied for any parameter. The elements within the  * collection, iterator, array, or varargs parameter list<i>may</i> be null --  * these will be represented in the output by the string {@code "null"}.  *  * @author Kevin Bourrillion  */
end_comment

begin_class
DECL|class|Join
specifier|public
specifier|final
class|class
name|Join
block|{
DECL|method|Join
specifier|private
name|Join
parameter_list|()
block|{     }
comment|/**      * Returns a string containing the {@code tokens}, converted to strings if      * necessary, separated by {@code delimiter}. If {@code tokens} is empty, it      * returns an empty string.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param delimiter a string to append between every element, but not at the      *                  beginning or end      * @param tokens    objects to append      * @return a string consisting of the joined elements      */
DECL|method|join
specifier|public
specifier|static
name|String
name|join
parameter_list|(
name|String
name|delimiter
parameter_list|,
name|Iterable
argument_list|<
name|?
argument_list|>
name|tokens
parameter_list|)
block|{
return|return
name|join
argument_list|(
name|delimiter
argument_list|,
name|tokens
operator|.
name|iterator
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns a string containing the {@code tokens}, converted to strings if      * necessary, separated by {@code delimiter}. If {@code tokens} is empty, it      * returns an empty string.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param delimiter a string to append between every element, but not at the      *                  beginning or end      * @param tokens    objects to append      * @return a string consisting of the joined elements      */
DECL|method|join
specifier|public
specifier|static
name|String
name|join
parameter_list|(
name|String
name|delimiter
parameter_list|,
name|Object
index|[]
name|tokens
parameter_list|)
block|{
return|return
name|join
argument_list|(
name|delimiter
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|tokens
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a string containing the {@code tokens}, converted to strings if      * necessary, separated by {@code delimiter}.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param delimiter   a string to append between every element, but not at the      *                    beginning or end      * @param firstToken  the first object to append      * @param otherTokens subsequent objects to append      * @return a string consisting of the joined elements      */
DECL|method|join
specifier|public
specifier|static
name|String
name|join
parameter_list|(
name|String
name|delimiter
parameter_list|,
annotation|@
name|Nullable
name|Object
name|firstToken
parameter_list|,
name|Object
modifier|...
name|otherTokens
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|otherTokens
argument_list|)
expr_stmt|;
return|return
name|join
argument_list|(
name|delimiter
argument_list|,
name|CollectionUtils
operator|.
name|asArrayList
argument_list|(
name|firstToken
argument_list|,
name|otherTokens
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a string containing the {@code tokens}, converted to strings if      * necessary, separated by {@code delimiter}. If {@code tokens} is empty, it      * returns an empty string.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param delimiter a string to append between every element, but not at the      *                  beginning or end      * @param tokens    objects to append      * @return a string consisting of the joined elements      */
DECL|method|join
specifier|public
specifier|static
name|String
name|join
parameter_list|(
name|String
name|delimiter
parameter_list|,
name|Iterator
argument_list|<
name|?
argument_list|>
name|tokens
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|join
argument_list|(
name|sb
argument_list|,
name|delimiter
argument_list|,
name|tokens
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Returns a string containing the contents of {@code map}, with entries      * separated by {@code entryDelimiter}, and keys and values separated with      * {@code keyValueSeparator}.      *<p>      * Each key and value will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param keyValueSeparator a string to append between every key and its      *                          associated value      * @param entryDelimiter    a string to append between every entry, but not at      *                          the beginning or end      * @param map               the map containing the data to join      * @return a string consisting of the joined entries of the map; empty if the      *         map is empty      */
DECL|method|join
specifier|public
specifier|static
name|String
name|join
parameter_list|(
name|String
name|keyValueSeparator
parameter_list|,
name|String
name|entryDelimiter
parameter_list|,
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
parameter_list|)
block|{
return|return
name|join
argument_list|(
operator|new
name|StringBuilder
argument_list|()
argument_list|,
name|keyValueSeparator
argument_list|,
name|entryDelimiter
argument_list|,
name|map
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Appends each of the {@code tokens} to {@code appendable}, separated by      * {@code delimiter}.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param appendable the object to append the results to      * @param delimiter  a string to append between every element, but not at the      *                   beginning or end      * @param tokens     objects to append      * @return the same {@code Appendable} instance that was passed in      * @throws JoinException if an {@link IOException} occurs      */
DECL|method|join
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Appendable
parameter_list|>
name|T
name|join
parameter_list|(
name|T
name|appendable
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|Iterable
argument_list|<
name|?
argument_list|>
name|tokens
parameter_list|)
block|{
return|return
name|join
argument_list|(
name|appendable
argument_list|,
name|delimiter
argument_list|,
name|tokens
operator|.
name|iterator
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Appends each of the {@code tokens} to {@code appendable}, separated by      * {@code delimiter}.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param appendable the object to append the results to      * @param delimiter  a string to append between every element, but not at the      *                   beginning or end      * @param tokens     objects to append      * @return the same {@code Appendable} instance that was passed in      * @throws JoinException if an {@link IOException} occurs      */
DECL|method|join
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Appendable
parameter_list|>
name|T
name|join
parameter_list|(
name|T
name|appendable
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|Object
index|[]
name|tokens
parameter_list|)
block|{
return|return
name|join
argument_list|(
name|appendable
argument_list|,
name|delimiter
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|tokens
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Appends each of the {@code tokens} to {@code appendable}, separated by      * {@code delimiter}.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param appendable  the object to append the results to      * @param delimiter   a string to append between every element, but not at the      *                    beginning or end      * @param firstToken  the first object to append      * @param otherTokens subsequent objects to append      * @return the same {@code Appendable} instance that was passed in      * @throws JoinException if an {@link IOException} occurs      */
DECL|method|join
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Appendable
parameter_list|>
name|T
name|join
parameter_list|(
name|T
name|appendable
parameter_list|,
name|String
name|delimiter
parameter_list|,
annotation|@
name|Nullable
name|Object
name|firstToken
parameter_list|,
name|Object
modifier|...
name|otherTokens
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|otherTokens
argument_list|)
expr_stmt|;
return|return
name|join
argument_list|(
name|appendable
argument_list|,
name|delimiter
argument_list|,
name|CollectionUtils
operator|.
name|asArrayList
argument_list|(
name|firstToken
argument_list|,
name|otherTokens
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Appends each of the {@code tokens} to {@code appendable}, separated by      * {@code delimiter}.      *<p>      * Each token will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param appendable the object to append the results to      * @param delimiter  a string to append between every element, but not at the      *                   beginning or end      * @param tokens     objects to append      * @return the same {@code Appendable} instance that was passed in      * @throws JoinException if an {@link IOException} occurs      */
DECL|method|join
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Appendable
parameter_list|>
name|T
name|join
parameter_list|(
name|T
name|appendable
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|Iterator
argument_list|<
name|?
argument_list|>
name|tokens
parameter_list|)
block|{
comment|/* This method is the workhorse of the class */
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|appendable
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|delimiter
argument_list|)
expr_stmt|;
if|if
condition|(
name|tokens
operator|.
name|hasNext
argument_list|()
condition|)
block|{
try|try
block|{
name|appendOneToken
argument_list|(
name|appendable
argument_list|,
name|tokens
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|tokens
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|appendable
operator|.
name|append
argument_list|(
name|delimiter
argument_list|)
expr_stmt|;
name|appendOneToken
argument_list|(
name|appendable
argument_list|,
name|tokens
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|JoinException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|appendable
return|;
block|}
comment|/**      * Appends the contents of {@code map} to {@code appendable}, with entries      * separated by {@code entryDelimiter}, and keys and values separated with      * {@code keyValueSeparator}.      *<p>      * Each key and value will be converted to a {@link CharSequence} using      * {@link String#valueOf(Object)}, if it isn't a {@link CharSequence} already.      * Note that this implies that null tokens will be appended as the      * four-character string {@code "null"}.      *      * @param appendable        the object to append the results to      * @param keyValueSeparator a string to append between every key and its      *                          associated value      * @param entryDelimiter    a string to append between every entry, but not at      *                          the beginning or end      * @param map               the map containing the data to join      * @return the same {@code Appendable} instance that was passed in      */
DECL|method|join
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Appendable
parameter_list|>
name|T
name|join
parameter_list|(
name|T
name|appendable
parameter_list|,
name|String
name|keyValueSeparator
parameter_list|,
name|String
name|entryDelimiter
parameter_list|,
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|appendable
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|keyValueSeparator
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|entryDelimiter
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|?
extends|extends
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
name|entries
init|=
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|entries
operator|.
name|hasNext
argument_list|()
condition|)
block|{
try|try
block|{
name|appendOneEntry
argument_list|(
name|appendable
argument_list|,
name|keyValueSeparator
argument_list|,
name|entries
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|entries
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|appendable
operator|.
name|append
argument_list|(
name|entryDelimiter
argument_list|)
expr_stmt|;
name|appendOneEntry
argument_list|(
name|appendable
argument_list|,
name|keyValueSeparator
argument_list|,
name|entries
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|JoinException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|appendable
return|;
block|}
DECL|method|appendOneEntry
specifier|private
specifier|static
name|void
name|appendOneEntry
parameter_list|(
name|Appendable
name|appendable
parameter_list|,
name|String
name|keyValueSeparator
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|appendOneToken
argument_list|(
name|appendable
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|appendable
operator|.
name|append
argument_list|(
name|keyValueSeparator
argument_list|)
expr_stmt|;
name|appendOneToken
argument_list|(
name|appendable
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|appendOneToken
specifier|private
specifier|static
name|void
name|appendOneToken
parameter_list|(
name|Appendable
name|appendable
parameter_list|,
name|Object
name|token
parameter_list|)
throws|throws
name|IOException
block|{
name|appendable
operator|.
name|append
argument_list|(
name|toCharSequence
argument_list|(
name|token
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|toCharSequence
specifier|private
specifier|static
name|CharSequence
name|toCharSequence
parameter_list|(
name|Object
name|token
parameter_list|)
block|{
return|return
operator|(
name|token
operator|instanceof
name|CharSequence
operator|)
condition|?
operator|(
name|CharSequence
operator|)
name|token
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|token
argument_list|)
return|;
block|}
comment|/**      * Exception thrown in response to an {@link IOException} from the supplied      * {@link Appendable}. This is used because most callers won't want to      * worry about catching an IOException.      */
DECL|class|JoinException
specifier|public
specifier|static
class|class
name|JoinException
extends|extends
name|RuntimeException
block|{
DECL|method|JoinException
specifier|private
name|JoinException
parameter_list|(
name|IOException
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
block|}
block|}
end_class

end_unit

