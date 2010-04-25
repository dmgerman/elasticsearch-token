begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|gcommon
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|MapMaker
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

begin_comment
comment|/**  * Lazily creates (and caches) values for keys. If creating the value fails (with errors), an  * exception is thrown on retrieval.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|FailableCache
specifier|public
specifier|abstract
class|class
name|FailableCache
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
DECL|field|delegate
specifier|private
specifier|final
name|Map
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
name|delegate
init|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|makeComputingMap
argument_list|(
operator|new
name|Function
argument_list|<
name|K
argument_list|,
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|apply
parameter_list|(
annotation|@
name|Nullable
name|K
name|key
parameter_list|)
block|{
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|()
decl_stmt|;
name|V
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|FailableCache
operator|.
name|this
operator|.
name|create
argument_list|(
name|key
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ErrorsException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|merge
argument_list|(
name|e
operator|.
name|getErrors
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|errors
operator|.
name|hasErrors
argument_list|()
condition|?
name|errors
else|:
name|result
return|;
block|}
block|}
argument_list|)
decl_stmt|;
DECL|method|create
specifier|protected
specifier|abstract
name|V
name|create
parameter_list|(
name|K
name|key
parameter_list|,
name|Errors
name|errors
parameter_list|)
throws|throws
name|ErrorsException
function_decl|;
DECL|method|get
specifier|public
name|V
name|get
parameter_list|(
name|K
name|key
parameter_list|,
name|Errors
name|errors
parameter_list|)
throws|throws
name|ErrorsException
block|{
name|Object
name|resultOrError
init|=
name|delegate
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|resultOrError
operator|instanceof
name|Errors
condition|)
block|{
name|errors
operator|.
name|merge
argument_list|(
operator|(
name|Errors
operator|)
name|resultOrError
argument_list|)
expr_stmt|;
throw|throw
name|errors
operator|.
name|toException
argument_list|()
throw|;
block|}
else|else
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// create returned a non-error result, so this is safe
name|V
name|result
init|=
operator|(
name|V
operator|)
name|resultOrError
decl_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

