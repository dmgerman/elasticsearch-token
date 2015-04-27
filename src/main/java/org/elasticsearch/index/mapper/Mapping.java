begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesReference
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|ToXContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|object
operator|.
name|RootObjectMapper
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
comment|/**  * Wrapper around everything that defines a mapping, without references to  * utility classes like MapperService, ...  */
end_comment

begin_class
DECL|class|Mapping
specifier|public
specifier|final
class|class
name|Mapping
implements|implements
name|ToXContent
block|{
comment|/**      * Transformations to be applied to the source before indexing and/or after loading.      */
DECL|interface|SourceTransform
specifier|public
interface|interface
name|SourceTransform
extends|extends
name|ToXContent
block|{
comment|/**          * Transform the source when it is expressed as a map.  This is public so it can be transformed the source is loaded.          * @param sourceAsMap source to transform.  This may be mutated by the script.          * @return transformed version of transformMe.  This may actually be the same object as sourceAsMap          */
DECL|method|transformSourceAsMap
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|transformSourceAsMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
parameter_list|)
function_decl|;
block|}
DECL|field|root
specifier|final
name|RootObjectMapper
name|root
decl_stmt|;
DECL|field|rootMappers
specifier|final
name|RootMapper
index|[]
name|rootMappers
decl_stmt|;
DECL|field|rootMappersNotIncludedInObject
specifier|final
name|RootMapper
index|[]
name|rootMappersNotIncludedInObject
decl_stmt|;
DECL|field|rootMappersMap
specifier|final
name|ImmutableMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|RootMapper
argument_list|>
argument_list|,
name|RootMapper
argument_list|>
name|rootMappersMap
decl_stmt|;
DECL|field|sourceTransforms
specifier|final
name|SourceTransform
index|[]
name|sourceTransforms
decl_stmt|;
DECL|field|meta
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|meta
decl_stmt|;
DECL|method|Mapping
specifier|public
name|Mapping
parameter_list|(
name|RootObjectMapper
name|rootObjectMapper
parameter_list|,
name|RootMapper
index|[]
name|rootMappers
parameter_list|,
name|SourceTransform
index|[]
name|sourceTransforms
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|meta
parameter_list|)
block|{
name|this
operator|.
name|root
operator|=
name|rootObjectMapper
expr_stmt|;
name|this
operator|.
name|rootMappers
operator|=
name|rootMappers
expr_stmt|;
name|List
argument_list|<
name|RootMapper
argument_list|>
name|rootMappersNotIncludedInObject
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|RootMapper
argument_list|>
argument_list|,
name|RootMapper
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|RootMapper
name|rootMapper
range|:
name|rootMappers
control|)
block|{
if|if
condition|(
name|rootMapper
operator|.
name|includeInObject
argument_list|()
condition|)
block|{
name|root
operator|.
name|putMapper
argument_list|(
name|rootMapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rootMappersNotIncludedInObject
operator|.
name|add
argument_list|(
name|rootMapper
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|put
argument_list|(
name|rootMapper
operator|.
name|getClass
argument_list|()
argument_list|,
name|rootMapper
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|rootMappersNotIncludedInObject
operator|=
name|rootMappersNotIncludedInObject
operator|.
name|toArray
argument_list|(
operator|new
name|RootMapper
index|[
name|rootMappersNotIncludedInObject
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootMappersMap
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceTransforms
operator|=
name|sourceTransforms
expr_stmt|;
name|this
operator|.
name|meta
operator|=
name|meta
expr_stmt|;
block|}
comment|/** Return the root object mapper. */
DECL|method|root
specifier|public
name|RootObjectMapper
name|root
parameter_list|()
block|{
return|return
name|root
return|;
block|}
comment|/**      * Generate a mapping update for the given root object mapper.      */
DECL|method|mappingUpdate
specifier|public
name|Mapping
name|mappingUpdate
parameter_list|(
name|Mapper
name|rootObjectMapper
parameter_list|)
block|{
return|return
operator|new
name|Mapping
argument_list|(
operator|(
name|RootObjectMapper
operator|)
name|rootObjectMapper
argument_list|,
name|rootMappers
argument_list|,
name|sourceTransforms
argument_list|,
name|meta
argument_list|)
return|;
block|}
comment|/** Get the root mapper with the given class. */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|rootMapper
specifier|public
parameter_list|<
name|T
extends|extends
name|RootMapper
parameter_list|>
name|T
name|rootMapper
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
operator|(
name|T
operator|)
name|rootMappersMap
operator|.
name|get
argument_list|(
name|clazz
argument_list|)
return|;
block|}
comment|/** @see DocumentMapper#merge(Mapping, boolean) */
DECL|method|merge
specifier|public
name|void
name|merge
parameter_list|(
name|Mapping
name|mergeWith
parameter_list|,
name|MergeResult
name|mergeResult
parameter_list|)
block|{
assert|assert
name|rootMappers
operator|.
name|length
operator|==
name|mergeWith
operator|.
name|rootMappers
operator|.
name|length
assert|;
name|root
operator|.
name|merge
argument_list|(
name|mergeWith
operator|.
name|root
argument_list|,
name|mergeResult
argument_list|)
expr_stmt|;
for|for
control|(
name|RootMapper
name|rootMapper
range|:
name|rootMappers
control|)
block|{
comment|// root mappers included in root object will get merge in the rootObjectMapper
if|if
condition|(
name|rootMapper
operator|.
name|includeInObject
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|RootMapper
name|mergeWithRootMapper
init|=
name|mergeWith
operator|.
name|rootMapper
argument_list|(
name|rootMapper
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mergeWithRootMapper
operator|!=
literal|null
condition|)
block|{
name|rootMapper
operator|.
name|merge
argument_list|(
name|mergeWithRootMapper
argument_list|,
name|mergeResult
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|mergeResult
operator|.
name|simulate
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// let the merge with attributes to override the attributes
name|meta
operator|=
name|mergeWith
operator|.
name|meta
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|root
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
operator|new
name|ToXContent
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|sourceTransforms
operator|.
name|length
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|sourceTransforms
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"transform"
argument_list|)
expr_stmt|;
name|sourceTransforms
index|[
literal|0
index|]
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"transform"
argument_list|)
expr_stmt|;
for|for
control|(
name|SourceTransform
name|transform
range|:
name|sourceTransforms
control|)
block|{
name|transform
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|meta
operator|!=
literal|null
operator|&&
operator|!
name|meta
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_meta"
argument_list|,
name|meta
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
comment|// no need to pass here id and boost, since they are added to the root object mapper
comment|// in the constructor
block|}
argument_list|,
name|rootMappersNotIncludedInObject
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
comment|/** Serialize to a {@link BytesReference}. */
DECL|method|toBytes
specifier|public
name|BytesReference
name|toBytes
parameter_list|()
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|bogus
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|bogus
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|bogus
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|bogus
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

