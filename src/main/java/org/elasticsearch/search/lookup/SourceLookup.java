begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.lookup
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|collect
operator|.
name|Tuple
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
name|XContentHelper
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
name|XContentType
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
name|support
operator|.
name|XContentMapValues
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
name|fieldvisitor
operator|.
name|JustSourceFieldsVisitor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SourceLookup
specifier|public
class|class
name|SourceLookup
implements|implements
name|Map
block|{
DECL|field|reader
specifier|private
name|LeafReader
name|reader
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|sourceAsBytes
specifier|private
name|BytesReference
name|sourceAsBytes
decl_stmt|;
DECL|field|source
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
decl_stmt|;
DECL|field|sourceContentType
specifier|private
name|XContentType
name|sourceContentType
decl_stmt|;
DECL|method|source
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|sourceContentType
specifier|public
name|XContentType
name|sourceContentType
parameter_list|()
block|{
return|return
name|sourceContentType
return|;
block|}
DECL|method|loadSourceIfNeeded
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|loadSourceIfNeeded
parameter_list|()
block|{
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
return|return
name|source
return|;
block|}
if|if
condition|(
name|sourceAsBytes
operator|!=
literal|null
condition|)
block|{
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|tuple
init|=
name|sourceAsMapAndType
argument_list|(
name|sourceAsBytes
argument_list|)
decl_stmt|;
name|sourceContentType
operator|=
name|tuple
operator|.
name|v1
argument_list|()
expr_stmt|;
name|source
operator|=
name|tuple
operator|.
name|v2
argument_list|()
expr_stmt|;
return|return
name|source
return|;
block|}
try|try
block|{
name|JustSourceFieldsVisitor
name|sourceFieldVisitor
init|=
operator|new
name|JustSourceFieldsVisitor
argument_list|()
decl_stmt|;
name|reader
operator|.
name|document
argument_list|(
name|docId
argument_list|,
name|sourceFieldVisitor
argument_list|)
expr_stmt|;
name|BytesReference
name|source
init|=
name|sourceFieldVisitor
operator|.
name|source
argument_list|()
decl_stmt|;
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|source
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceContentType
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|tuple
init|=
name|sourceAsMapAndType
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|this
operator|.
name|sourceContentType
operator|=
name|tuple
operator|.
name|v1
argument_list|()
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|tuple
operator|.
name|v2
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse / load source"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|sourceAsMapAndType
specifier|public
specifier|static
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|sourceAsMapAndType
parameter_list|(
name|BytesReference
name|source
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
return|return
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|source
argument_list|,
literal|false
argument_list|)
return|;
block|}
DECL|method|sourceAsMap
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
parameter_list|(
name|BytesReference
name|source
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
return|return
name|sourceAsMapAndType
argument_list|(
name|source
argument_list|)
operator|.
name|v2
argument_list|()
return|;
block|}
DECL|method|sourceAsMapAndType
specifier|public
specifier|static
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|sourceAsMapAndType
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
return|return
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
DECL|method|sourceAsMap
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
return|return
name|sourceAsMapAndType
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|.
name|v2
argument_list|()
return|;
block|}
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|reader
operator|==
name|context
operator|.
name|reader
argument_list|()
condition|)
block|{
comment|// if we are called with the same reader, don't invalidate source
return|return;
block|}
name|this
operator|.
name|reader
operator|=
name|context
operator|.
name|reader
argument_list|()
expr_stmt|;
name|this
operator|.
name|source
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|sourceAsBytes
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|docId
operator|=
operator|-
literal|1
expr_stmt|;
block|}
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|docId
operator|==
name|docId
condition|)
block|{
comment|// if we are called with the same docId, don't invalidate source
return|return;
block|}
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|this
operator|.
name|sourceAsBytes
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|source
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|setNextSource
specifier|public
name|void
name|setNextSource
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|sourceAsBytes
operator|=
name|source
expr_stmt|;
block|}
DECL|method|setNextSourceContentType
specifier|public
name|void
name|setNextSourceContentType
parameter_list|(
name|XContentType
name|sourceContentType
parameter_list|)
block|{
name|this
operator|.
name|sourceContentType
operator|=
name|sourceContentType
expr_stmt|;
block|}
DECL|method|setNextSource
specifier|public
name|void
name|setNextSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
comment|/**      * Internal source representation, might be compressed....      */
DECL|method|internalSourceRef
specifier|public
name|BytesReference
name|internalSourceRef
parameter_list|()
block|{
return|return
name|sourceAsBytes
return|;
block|}
comment|/**      * Returns the values associated with the path. Those are "low" level values, and it can      * handle path expression where an array/list is navigated within.      */
DECL|method|extractRawValues
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|extractRawValues
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|XContentMapValues
operator|.
name|extractRawValues
argument_list|(
name|path
argument_list|,
name|loadSourceIfNeeded
argument_list|()
argument_list|)
return|;
block|}
DECL|method|filter
specifier|public
name|Object
name|filter
parameter_list|(
name|String
index|[]
name|includes
parameter_list|,
name|String
index|[]
name|excludes
parameter_list|)
block|{
return|return
name|XContentMapValues
operator|.
name|filter
argument_list|(
name|loadSourceIfNeeded
argument_list|()
argument_list|,
name|includes
argument_list|,
name|excludes
argument_list|)
return|;
block|}
DECL|method|extractValue
specifier|public
name|Object
name|extractValue
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
name|path
argument_list|,
name|loadSourceIfNeeded
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|keySet
specifier|public
name|Set
name|keySet
parameter_list|()
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|keySet
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|values
specifier|public
name|Collection
name|values
parameter_list|()
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|values
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|entrySet
specifier|public
name|Set
name|entrySet
parameter_list|()
block|{
return|return
name|loadSourceIfNeeded
argument_list|()
operator|.
name|entrySet
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|put
specifier|public
name|Object
name|put
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
name|Object
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
name|m
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

