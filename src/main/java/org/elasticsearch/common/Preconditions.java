begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchNullPointerException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * Simple static methods to be called at the start of your own methods to verify  * correct arguments and state. This allows constructs such as  *<pre>  *     if (count<= 0) {  *       throw new ElasticsearchIllegalArgumentException("must be positive: " + count);  *     }</pre>  *  * to be replaced with the more compact  *<pre>  *     checkArgument(count> 0, "must be positive: %s", count);</pre>  *  * Note that the sense of the expression is inverted; with {@code Preconditions}  * you declare what you expect to be<i>true</i>, just as you do with an  *<a href="http://java.sun.com/j2se/1.5.0/docs/guide/language/assert.html">  * {@code assert}</a> or a JUnit {@code assertTrue()} call.  *  *<p>Take care not to confuse precondition checking with other similar types  * of checks! Precondition exceptions -- including those provided here, but also  * {@link IndexOutOfBoundsException}, {@link NoSuchElementException}, {@link  * UnsupportedOperationException} and others -- are used to signal that the  *<i>calling method</i> has made an error. This tells the caller that it should  * not have invoked the method when it did, with the arguments it did, or  * perhaps<i>ever</i>. Postcondition or other invariant failures should not  * throw these types of exceptions.  *  *<p><b>Note:</b> The methods of the {@code Preconditions} class are highly  * unusual in one way: they are<i>supposed to</i> throw exceptions, and promise  * in their specifications to do so even when given perfectly valid input. That  * is, {@code null} is a valid parameter to the method {@link  * #checkNotNull(Object)} -- and technically this parameter could be even marked  * as Nullable -- yet the method will still throw an exception anyway,  * because that's what its contract says to do.  *  *  */
end_comment

begin_class
DECL|class|Preconditions
specifier|public
specifier|final
class|class
name|Preconditions
block|{
DECL|method|Preconditions
specifier|private
name|Preconditions
parameter_list|()
block|{     }
comment|/**      * Ensures the truth of an expression involving one or more parameters to the      * calling method.      *      * @param expression a boolean expression      * @throws org.elasticsearch.ElasticsearchIllegalArgumentException      *          if {@code expression} is false      */
DECL|method|checkArgument
specifier|public
specifier|static
name|void
name|checkArgument
parameter_list|(
name|boolean
name|expression
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|()
throw|;
block|}
block|}
comment|/**      * Ensures the truth of an expression involving one or more parameters to the      * calling method.      *      * @param expression   a boolean expression      * @param errorMessage the exception message to use if the check fails; will      *                     be converted to a string using {@link String#valueOf(Object)}      * @throws org.elasticsearch.ElasticsearchIllegalArgumentException      *          if {@code expression} is false      */
DECL|method|checkArgument
specifier|public
specifier|static
name|void
name|checkArgument
parameter_list|(
name|boolean
name|expression
parameter_list|,
name|Object
name|errorMessage
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|errorMessage
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**      * Ensures the truth of an expression involving one or more parameters to the      * calling method.      *      * @param expression           a boolean expression      * @param errorMessageTemplate a template for the exception message should the      *                             check fail. The message is formed by replacing each {@code %s}      *                             placeholder in the template with an argument. These are matched by      *                             position - the first {@code %s} gets {@code errorMessageArgs[0]}, etc.      *                             Unmatched arguments will be appended to the formatted message in square      *                             braces. Unmatched placeholders will be left as-is.      * @param errorMessageArgs     the arguments to be substituted into the message      *                             template. Arguments are converted to strings using      *                             {@link String#valueOf(Object)}.      * @throws org.elasticsearch.ElasticsearchIllegalArgumentException      *          if {@code expression} is false      * @throws org.elasticsearch.ElasticsearchNullPointerException      *          if the check fails and either {@code      *          errorMessageTemplate} or {@code errorMessageArgs} is null (don't let      *          this happen)      */
DECL|method|checkArgument
specifier|public
specifier|static
name|void
name|checkArgument
parameter_list|(
name|boolean
name|expression
parameter_list|,
name|String
name|errorMessageTemplate
parameter_list|,
name|Object
modifier|...
name|errorMessageArgs
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
name|format
argument_list|(
name|errorMessageTemplate
argument_list|,
name|errorMessageArgs
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**      * Ensures the truth of an expression involving the state of the calling      * instance, but not involving any parameters to the calling method.      *      * @param expression a boolean expression      * @throws org.elasticsearch.ElasticsearchIllegalStateException      *          if {@code expression} is false      */
DECL|method|checkState
specifier|public
specifier|static
name|void
name|checkState
parameter_list|(
name|boolean
name|expression
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|()
throw|;
block|}
block|}
comment|/**      * Ensures the truth of an expression involving the state of the calling      * instance, but not involving any parameters to the calling method.      *      * @param expression   a boolean expression      * @param errorMessage the exception message to use if the check fails; will      *                     be converted to a string using {@link String#valueOf(Object)}      * @throws org.elasticsearch.ElasticsearchIllegalStateException      *          if {@code expression} is false      */
DECL|method|checkState
specifier|public
specifier|static
name|void
name|checkState
parameter_list|(
name|boolean
name|expression
parameter_list|,
name|Object
name|errorMessage
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|errorMessage
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**      * Ensures the truth of an expression involving the state of the calling      * instance, but not involving any parameters to the calling method.      *      * @param expression           a boolean expression      * @param errorMessageTemplate a template for the exception message should the      *                             check fail. The message is formed by replacing each {@code %s}      *                             placeholder in the template with an argument. These are matched by      *                             position - the first {@code %s} gets {@code errorMessageArgs[0]}, etc.      *                             Unmatched arguments will be appended to the formatted message in square      *                             braces. Unmatched placeholders will be left as-is.      * @param errorMessageArgs     the arguments to be substituted into the message      *                             template. Arguments are converted to strings using      *                             {@link String#valueOf(Object)}.      * @throws org.elasticsearch.ElasticsearchIllegalStateException      *          if {@code expression} is false      * @throws org.elasticsearch.ElasticsearchNullPointerException      *          if the check fails and either {@code      *          errorMessageTemplate} or {@code errorMessageArgs} is null (don't let      *          this happen)      */
DECL|method|checkState
specifier|public
specifier|static
name|void
name|checkState
parameter_list|(
name|boolean
name|expression
parameter_list|,
name|String
name|errorMessageTemplate
parameter_list|,
name|Object
modifier|...
name|errorMessageArgs
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
name|format
argument_list|(
name|errorMessageTemplate
argument_list|,
name|errorMessageArgs
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**      * Ensures that an object reference passed as a parameter to the calling      * method is not null.      *      * @param reference an object reference      * @return the non-null reference that was validated      * @throws org.elasticsearch.ElasticsearchNullPointerException      *          if {@code reference} is null      */
DECL|method|checkNotNull
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|checkNotNull
parameter_list|(
name|T
name|reference
parameter_list|)
block|{
if|if
condition|(
name|reference
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchNullPointerException
argument_list|()
throw|;
block|}
return|return
name|reference
return|;
block|}
comment|/**      * Ensures that an object reference passed as a parameter to the calling      * method is not null.      *      * @param reference    an object reference      * @param errorMessage the exception message to use if the check fails; will      *                     be converted to a string using {@link String#valueOf(Object)}      * @return the non-null reference that was validated      * @throws org.elasticsearch.ElasticsearchNullPointerException      *          if {@code reference} is null      */
DECL|method|checkNotNull
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|checkNotNull
parameter_list|(
name|T
name|reference
parameter_list|,
name|Object
name|errorMessage
parameter_list|)
block|{
if|if
condition|(
name|reference
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchNullPointerException
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|errorMessage
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|reference
return|;
block|}
comment|/**      * Ensures that an object reference passed as a parameter to the calling      * method is not null.      *      * @param reference            an object reference      * @param errorMessageTemplate a template for the exception message should the      *                             check fail. The message is formed by replacing each {@code %s}      *                             placeholder in the template with an argument. These are matched by      *                             position - the first {@code %s} gets {@code errorMessageArgs[0]}, etc.      *                             Unmatched arguments will be appended to the formatted message in square      *                             braces. Unmatched placeholders will be left as-is.      * @param errorMessageArgs     the arguments to be substituted into the message      *                             template. Arguments are converted to strings using      *                             {@link String#valueOf(Object)}.      * @return the non-null reference that was validated      * @throws org.elasticsearch.ElasticsearchNullPointerException      *          if {@code reference} is null      */
DECL|method|checkNotNull
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|checkNotNull
parameter_list|(
name|T
name|reference
parameter_list|,
name|String
name|errorMessageTemplate
parameter_list|,
name|Object
modifier|...
name|errorMessageArgs
parameter_list|)
block|{
if|if
condition|(
name|reference
operator|==
literal|null
condition|)
block|{
comment|// If either of these parameters is null, the right thing happens anyway
throw|throw
operator|new
name|ElasticsearchNullPointerException
argument_list|(
name|format
argument_list|(
name|errorMessageTemplate
argument_list|,
name|errorMessageArgs
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|reference
return|;
block|}
comment|/**      * Substitutes each {@code %s} in {@code template} with an argument. These      * are matched by position - the first {@code %s} gets {@code args[0]}, etc.      * If there are more arguments than placeholders, the unmatched arguments will      * be appended to the end of the formatted message in square braces.      *      * @param template a non-null string containing 0 or more {@code %s}      *                 placeholders.      * @param args     the arguments to be substituted into the message      *                 template. Arguments are converted to strings using      *                 {@link String#valueOf(Object)}. Arguments can be null.      */
comment|// VisibleForTesting
DECL|method|format
specifier|static
name|String
name|format
parameter_list|(
name|String
name|template
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
comment|// start substituting the arguments into the '%s' placeholders
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
name|template
operator|.
name|length
argument_list|()
operator|+
literal|16
operator|*
name|args
operator|.
name|length
argument_list|)
decl_stmt|;
name|int
name|templateStart
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|args
operator|.
name|length
condition|)
block|{
name|int
name|placeholderStart
init|=
name|template
operator|.
name|indexOf
argument_list|(
literal|"%s"
argument_list|,
name|templateStart
argument_list|)
decl_stmt|;
if|if
condition|(
name|placeholderStart
operator|==
operator|-
literal|1
condition|)
block|{
break|break;
block|}
name|builder
operator|.
name|append
argument_list|(
name|template
operator|.
name|substring
argument_list|(
name|templateStart
argument_list|,
name|placeholderStart
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|args
index|[
name|i
operator|++
index|]
argument_list|)
expr_stmt|;
name|templateStart
operator|=
name|placeholderStart
operator|+
literal|2
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|template
operator|.
name|substring
argument_list|(
name|templateStart
argument_list|)
argument_list|)
expr_stmt|;
comment|// if we run out of placeholders, append the extra args in square braces
if|if
condition|(
name|i
operator|<
name|args
operator|.
name|length
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|args
index|[
name|i
operator|++
index|]
argument_list|)
expr_stmt|;
while|while
condition|(
name|i
operator|<
name|args
operator|.
name|length
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|args
index|[
name|i
operator|++
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

