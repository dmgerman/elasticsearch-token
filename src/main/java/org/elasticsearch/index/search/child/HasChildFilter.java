begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.child
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|child
package|;
end_package

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|hash
operator|.
name|THashSet
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
name|AtomicReaderContext
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
name|IndexReader
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
name|Term
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
name|search
operator|.
name|DocIdSet
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
name|search
operator|.
name|Filter
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
name|search
operator|.
name|Query
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
name|util
operator|.
name|Bits
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
name|util
operator|.
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|HashedBytesArray
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
name|lucene
operator|.
name|docset
operator|.
name|MatchDocIdSet
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
name|lucene
operator|.
name|search
operator|.
name|Queries
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
name|lucene
operator|.
name|search
operator|.
name|TermFilter
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
name|recycler
operator|.
name|Recycler
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
name|cache
operator|.
name|id
operator|.
name|IdReaderTypeCache
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
name|Uid
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
name|internal
operator|.
name|UidFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|HasChildFilter
specifier|public
class|class
name|HasChildFilter
extends|extends
name|Filter
implements|implements
name|SearchContext
operator|.
name|Rewrite
block|{
DECL|field|childQuery
specifier|final
name|Query
name|childQuery
decl_stmt|;
DECL|field|parentType
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|childType
specifier|final
name|String
name|childType
decl_stmt|;
DECL|field|parentFilter
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|searchContext
specifier|final
name|SearchContext
name|searchContext
decl_stmt|;
DECL|field|shortCircuitParentDocSet
specifier|final
name|int
name|shortCircuitParentDocSet
decl_stmt|;
DECL|field|shortCircuitFilter
name|Filter
name|shortCircuitFilter
decl_stmt|;
DECL|field|remaining
name|int
name|remaining
decl_stmt|;
DECL|field|collectedUids
name|Recycler
operator|.
name|V
argument_list|<
name|THashSet
argument_list|<
name|HashedBytesArray
argument_list|>
argument_list|>
name|collectedUids
decl_stmt|;
DECL|method|HasChildFilter
specifier|public
name|HasChildFilter
parameter_list|(
name|Query
name|childQuery
parameter_list|,
name|String
name|parentType
parameter_list|,
name|String
name|childType
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|,
name|int
name|shortCircuitParentDocSet
parameter_list|)
block|{
name|this
operator|.
name|parentFilter
operator|=
name|parentFilter
expr_stmt|;
name|this
operator|.
name|searchContext
operator|=
name|searchContext
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|childType
operator|=
name|childType
expr_stmt|;
name|this
operator|.
name|childQuery
operator|=
name|childQuery
expr_stmt|;
name|this
operator|.
name|shortCircuitParentDocSet
operator|=
name|shortCircuitParentDocSet
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|this
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HasChildFilter
name|that
init|=
operator|(
name|HasChildFilter
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|childQuery
operator|.
name|equals
argument_list|(
name|that
operator|.
name|childQuery
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|childType
operator|.
name|equals
argument_list|(
name|that
operator|.
name|childType
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|childQuery
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|childType
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"child_filter["
argument_list|)
operator|.
name|append
argument_list|(
name|childType
argument_list|)
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
operator|.
name|append
argument_list|(
name|parentType
argument_list|)
operator|.
name|append
argument_list|(
literal|"]("
argument_list|)
operator|.
name|append
argument_list|(
name|childQuery
argument_list|)
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|getDocIdSet
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|collectedUids
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"has_child filter hasn't executed properly"
argument_list|)
throw|;
block|}
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|shortCircuitFilter
operator|!=
literal|null
condition|)
block|{
return|return
name|shortCircuitFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|acceptDocs
argument_list|)
return|;
block|}
name|DocIdSet
name|parentDocIdSet
init|=
name|this
operator|.
name|parentFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|parentDocIdSet
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Bits
name|parentsBits
init|=
name|DocIdSets
operator|.
name|toSafeBits
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|parentDocIdSet
argument_list|)
decl_stmt|;
name|IdReaderTypeCache
name|idReaderTypeCache
init|=
name|searchContext
operator|.
name|idCache
argument_list|()
operator|.
name|reader
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
if|if
condition|(
name|idReaderTypeCache
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|ParentDocSet
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|parentsBits
argument_list|,
name|collectedUids
operator|.
name|v
argument_list|()
argument_list|,
name|idReaderTypeCache
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|contextRewrite
specifier|public
name|void
name|contextRewrite
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|)
throws|throws
name|Exception
block|{
name|searchContext
operator|.
name|idCache
argument_list|()
operator|.
name|refresh
argument_list|(
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|getTopReaderContext
argument_list|()
operator|.
name|leaves
argument_list|()
argument_list|)
expr_stmt|;
name|collectedUids
operator|=
name|searchContext
operator|.
name|cacheRecycler
argument_list|()
operator|.
name|hashSet
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|UidCollector
name|collector
init|=
operator|new
name|UidCollector
argument_list|(
name|parentType
argument_list|,
name|searchContext
argument_list|,
name|collectedUids
operator|.
name|v
argument_list|()
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|childQuery
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|remaining
operator|=
name|collectedUids
operator|.
name|v
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
name|shortCircuitFilter
operator|=
name|Queries
operator|.
name|MATCH_NO_FILTER
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|remaining
operator|==
literal|1
condition|)
block|{
name|BytesRef
name|id
init|=
name|collectedUids
operator|.
name|v
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|toBytesRef
argument_list|()
decl_stmt|;
name|shortCircuitFilter
operator|=
operator|new
name|TermFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|parentType
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|remaining
operator|<=
name|shortCircuitParentDocSet
condition|)
block|{
name|shortCircuitFilter
operator|=
operator|new
name|ParentIdsFilter
argument_list|(
name|parentType
argument_list|,
name|collectedUids
operator|.
name|v
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|executionDone
specifier|public
name|void
name|executionDone
parameter_list|()
block|{
if|if
condition|(
name|collectedUids
operator|!=
literal|null
condition|)
block|{
name|collectedUids
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
name|collectedUids
operator|=
literal|null
expr_stmt|;
name|shortCircuitFilter
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contextClear
specifier|public
name|void
name|contextClear
parameter_list|()
block|{     }
DECL|class|ParentDocSet
specifier|final
class|class
name|ParentDocSet
extends|extends
name|MatchDocIdSet
block|{
DECL|field|reader
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|field|parents
specifier|final
name|THashSet
argument_list|<
name|HashedBytesArray
argument_list|>
name|parents
decl_stmt|;
DECL|field|typeCache
specifier|final
name|IdReaderTypeCache
name|typeCache
decl_stmt|;
DECL|method|ParentDocSet
name|ParentDocSet
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|,
name|THashSet
argument_list|<
name|HashedBytesArray
argument_list|>
name|parents
parameter_list|,
name|IdReaderTypeCache
name|typeCache
parameter_list|)
block|{
name|super
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|acceptDocs
argument_list|)
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|parents
operator|=
name|parents
expr_stmt|;
name|this
operator|.
name|typeCache
operator|=
name|typeCache
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matchDoc
specifier|protected
name|boolean
name|matchDoc
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
name|shortCircuit
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|boolean
name|match
init|=
name|parents
operator|.
name|contains
argument_list|(
name|typeCache
operator|.
name|idByDoc
argument_list|(
name|doc
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|match
condition|)
block|{
name|remaining
operator|--
expr_stmt|;
block|}
return|return
name|match
return|;
block|}
block|}
DECL|class|UidCollector
specifier|final
specifier|static
class|class
name|UidCollector
extends|extends
name|ParentIdCollector
block|{
DECL|field|collectedUids
specifier|final
name|THashSet
argument_list|<
name|HashedBytesArray
argument_list|>
name|collectedUids
decl_stmt|;
DECL|method|UidCollector
name|UidCollector
parameter_list|(
name|String
name|parentType
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|THashSet
argument_list|<
name|HashedBytesArray
argument_list|>
name|collectedUids
parameter_list|)
block|{
name|super
argument_list|(
name|parentType
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|collectedUids
operator|=
name|collectedUids
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|HashedBytesArray
name|parentIdByDoc
parameter_list|)
block|{
name|collectedUids
operator|.
name|add
argument_list|(
name|parentIdByDoc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

