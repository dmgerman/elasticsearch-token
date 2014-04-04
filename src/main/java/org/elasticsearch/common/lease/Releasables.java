begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lease
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lease
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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

begin_comment
comment|/** Utility methods to work with {@link Releasable}s. */
end_comment

begin_enum
DECL|enum|Releasables
specifier|public
enum|enum
name|Releasables
block|{     ;
DECL|method|rethrow
specifier|private
specifier|static
name|void
name|rethrow
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|RuntimeException
condition|)
block|{
throw|throw
operator|(
name|RuntimeException
operator|)
name|t
throw|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|Error
condition|)
block|{
throw|throw
operator|(
name|Error
operator|)
name|t
throw|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|t
argument_list|)
throw|;
block|}
DECL|method|close
specifier|private
specifier|static
name|void
name|close
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Releasable
argument_list|>
name|releasables
parameter_list|,
name|boolean
name|ignoreException
parameter_list|)
block|{
name|Throwable
name|th
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Releasable
name|releasable
range|:
name|releasables
control|)
block|{
if|if
condition|(
name|releasable
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|releasable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|th
operator|==
literal|null
condition|)
block|{
name|th
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|th
operator|!=
literal|null
operator|&&
operator|!
name|ignoreException
condition|)
block|{
name|rethrow
argument_list|(
name|th
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Release the provided {@link Releasable}s. */
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Releasable
argument_list|>
name|releasables
parameter_list|)
block|{
name|close
argument_list|(
name|releasables
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/** Release the provided {@link Releasable}s. */
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|(
name|Releasable
modifier|...
name|releasables
parameter_list|)
block|{
name|close
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|releasables
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Release the provided {@link Releasable}s, ignoring exceptions. */
DECL|method|closeWhileHandlingException
specifier|public
specifier|static
name|void
name|closeWhileHandlingException
parameter_list|(
name|Iterable
argument_list|<
name|Releasable
argument_list|>
name|releasables
parameter_list|)
block|{
name|close
argument_list|(
name|releasables
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/** Release the provided {@link Releasable}s, ignoring exceptions. */
DECL|method|closeWhileHandlingException
specifier|public
specifier|static
name|void
name|closeWhileHandlingException
parameter_list|(
name|Releasable
modifier|...
name|releasables
parameter_list|)
block|{
name|closeWhileHandlingException
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|releasables
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Release the provided {@link Releasable}s, ignoring exceptions if<code>success</code> is<tt>false</tt>. */
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|(
name|boolean
name|success
parameter_list|,
name|Iterable
argument_list|<
name|Releasable
argument_list|>
name|releasables
parameter_list|)
block|{
if|if
condition|(
name|success
condition|)
block|{
name|close
argument_list|(
name|releasables
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|closeWhileHandlingException
argument_list|(
name|releasables
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Release the provided {@link Releasable}s, ignoring exceptions if<code>success</code> is<tt>false</tt>. */
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|(
name|boolean
name|success
parameter_list|,
name|Releasable
modifier|...
name|releasables
parameter_list|)
block|{
name|close
argument_list|(
name|success
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|releasables
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Wrap several releasables into a single one. This is typically useful for use with try-with-resources: for example let's assume      *  that you store in a list several resources that you would like to see released after execution of the try block:      *      *<pre>      *  List&lt;Releasable&gt; resources = ...;      *  try (Releasable releasable = Releasables.wrap(resources)) {      *      // do something      *  }      *  // the resources will be released when reaching here      *</pre>      */
DECL|method|wrap
specifier|public
specifier|static
name|Releasable
name|wrap
parameter_list|(
specifier|final
name|Iterable
argument_list|<
name|Releasable
argument_list|>
name|releasables
parameter_list|)
block|{
return|return
operator|new
name|Releasable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|releasables
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/** @see #wrap(Iterable) */
DECL|method|wrap
specifier|public
specifier|static
name|Releasable
name|wrap
parameter_list|(
specifier|final
name|Releasable
modifier|...
name|releasables
parameter_list|)
block|{
return|return
operator|new
name|Releasable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|releasables
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_enum

end_unit

