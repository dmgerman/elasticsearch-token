begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
package|;
end_package

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
name|DocIdSetIterator
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
name|FilteredDocIdSetIterator
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A filter that filters out deleted documents.  */
end_comment

begin_class
DECL|class|NotDeletedFilter
specifier|public
class|class
name|NotDeletedFilter
extends|extends
name|Filter
block|{
DECL|field|filter
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|method|NotDeletedFilter
specifier|public
name|NotDeletedFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
annotation|@
name|Override
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
name|DocIdSet
name|docIdSet
init|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|acceptDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|context
operator|.
name|reader
argument_list|()
operator|.
name|hasDeletions
argument_list|()
condition|)
block|{
return|return
name|docIdSet
return|;
block|}
return|return
operator|new
name|NotDeletedDocIdSet
argument_list|(
name|docIdSet
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
return|;
block|}
DECL|method|filter
specifier|public
name|Filter
name|filter
parameter_list|()
block|{
return|return
name|this
operator|.
name|filter
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
return|return
literal|"NotDeleted("
operator|+
name|filter
operator|+
literal|")"
return|;
block|}
DECL|class|NotDeletedDocIdSet
specifier|static
class|class
name|NotDeletedDocIdSet
extends|extends
name|DocIdSet
block|{
DECL|field|innerSet
specifier|private
specifier|final
name|DocIdSet
name|innerSet
decl_stmt|;
DECL|field|liveDocs
specifier|private
specifier|final
name|Bits
name|liveDocs
decl_stmt|;
DECL|method|NotDeletedDocIdSet
name|NotDeletedDocIdSet
parameter_list|(
name|DocIdSet
name|innerSet
parameter_list|,
name|Bits
name|liveDocs
parameter_list|)
block|{
name|this
operator|.
name|innerSet
operator|=
name|innerSet
expr_stmt|;
name|this
operator|.
name|liveDocs
operator|=
name|liveDocs
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|DocIdSetIterator
name|iterator
parameter_list|()
throws|throws
name|IOException
block|{
name|DocIdSetIterator
name|iterator
init|=
name|innerSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|iterator
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|NotDeletedDocIdSetIterator
argument_list|(
name|iterator
argument_list|,
name|liveDocs
argument_list|)
return|;
block|}
block|}
DECL|class|NotDeletedDocIdSetIterator
specifier|static
class|class
name|NotDeletedDocIdSetIterator
extends|extends
name|FilteredDocIdSetIterator
block|{
DECL|field|liveDocs
specifier|private
specifier|final
name|Bits
name|liveDocs
decl_stmt|;
DECL|method|NotDeletedDocIdSetIterator
name|NotDeletedDocIdSetIterator
parameter_list|(
name|DocIdSetIterator
name|innerIter
parameter_list|,
name|Bits
name|liveDocs
parameter_list|)
block|{
name|super
argument_list|(
name|innerIter
argument_list|)
expr_stmt|;
name|this
operator|.
name|liveDocs
operator|=
name|liveDocs
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|match
specifier|protected
name|boolean
name|match
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
return|return
name|liveDocs
operator|==
literal|null
operator|||
name|liveDocs
operator|.
name|get
argument_list|(
name|doc
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

