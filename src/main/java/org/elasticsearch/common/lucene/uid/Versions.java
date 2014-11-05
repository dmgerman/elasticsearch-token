begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.uid
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|uid
package|;
end_package

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
name|concurrent
operator|.
name|ConcurrentMap
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
name|*
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
operator|.
name|ReaderClosedListener
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
name|CloseableThreadLocal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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

begin_comment
comment|/** Utility class to resolve the Lucene doc ID and version for a given uid. */
end_comment

begin_class
DECL|class|Versions
specifier|public
class|class
name|Versions
block|{
DECL|field|MATCH_ANY
specifier|public
specifier|static
specifier|final
name|long
name|MATCH_ANY
init|=
operator|-
literal|3L
decl_stmt|;
comment|// Version was not specified by the user
comment|// TODO: can we remove this now?  rolling upgrades only need to handle prev (not older than that) version...?
comment|// the value for MATCH_ANY before ES 1.2.0 - will be removed
DECL|field|MATCH_ANY_PRE_1_2_0
specifier|public
specifier|static
specifier|final
name|long
name|MATCH_ANY_PRE_1_2_0
init|=
literal|0L
decl_stmt|;
DECL|field|NOT_FOUND
specifier|public
specifier|static
specifier|final
name|long
name|NOT_FOUND
init|=
operator|-
literal|1L
decl_stmt|;
DECL|field|NOT_SET
specifier|public
specifier|static
specifier|final
name|long
name|NOT_SET
init|=
operator|-
literal|2L
decl_stmt|;
comment|// TODO: is there somewhere else we can store these?
DECL|field|lookupStates
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|IndexReader
argument_list|,
name|CloseableThreadLocal
argument_list|<
name|PerThreadIDAndVersionLookup
argument_list|>
argument_list|>
name|lookupStates
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMapWithAggressiveConcurrency
argument_list|()
decl_stmt|;
comment|// Evict this reader from lookupStates once it's closed:
DECL|field|removeLookupState
specifier|private
specifier|static
specifier|final
name|ReaderClosedListener
name|removeLookupState
init|=
operator|new
name|ReaderClosedListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onClose
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|CloseableThreadLocal
argument_list|<
name|PerThreadIDAndVersionLookup
argument_list|>
name|ctl
init|=
name|lookupStates
operator|.
name|remove
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctl
operator|!=
literal|null
condition|)
block|{
name|ctl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
DECL|method|getLookupState
specifier|private
specifier|static
name|PerThreadIDAndVersionLookup
name|getLookupState
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|CloseableThreadLocal
argument_list|<
name|PerThreadIDAndVersionLookup
argument_list|>
name|ctl
init|=
name|lookupStates
operator|.
name|get
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctl
operator|==
literal|null
condition|)
block|{
comment|// First time we are seeing this reader; make a
comment|// new CTL:
name|ctl
operator|=
operator|new
name|CloseableThreadLocal
argument_list|<
name|PerThreadIDAndVersionLookup
argument_list|>
argument_list|()
expr_stmt|;
name|CloseableThreadLocal
argument_list|<
name|PerThreadIDAndVersionLookup
argument_list|>
name|other
init|=
name|lookupStates
operator|.
name|putIfAbsent
argument_list|(
name|reader
argument_list|,
name|ctl
argument_list|)
decl_stmt|;
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
comment|// Our CTL won, we must remove it when the
comment|// reader is closed:
name|reader
operator|.
name|addReaderClosedListener
argument_list|(
name|removeLookupState
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Another thread beat us to it: just use
comment|// their CTL:
name|ctl
operator|=
name|other
expr_stmt|;
block|}
block|}
name|PerThreadIDAndVersionLookup
name|lookupState
init|=
name|ctl
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|lookupState
operator|==
literal|null
condition|)
block|{
name|lookupState
operator|=
operator|new
name|PerThreadIDAndVersionLookup
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|ctl
operator|.
name|set
argument_list|(
name|lookupState
argument_list|)
expr_stmt|;
block|}
return|return
name|lookupState
return|;
block|}
DECL|method|writeVersion
specifier|public
specifier|static
name|void
name|writeVersion
parameter_list|(
name|long
name|version
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
operator|&&
name|version
operator|==
name|MATCH_ANY
condition|)
block|{
comment|// we have to send out a value the node will understand
name|version
operator|=
name|MATCH_ANY_PRE_1_2_0
expr_stmt|;
block|}
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
DECL|method|readVersion
specifier|public
specifier|static
name|long
name|readVersion
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|version
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
operator|&&
name|version
operator|==
name|MATCH_ANY_PRE_1_2_0
condition|)
block|{
name|version
operator|=
name|MATCH_ANY
expr_stmt|;
block|}
return|return
name|version
return|;
block|}
DECL|method|writeVersionWithVLongForBW
specifier|public
specifier|static
name|void
name|writeVersionWithVLongForBW
parameter_list|(
name|long
name|version
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|version
operator|==
name|MATCH_ANY
condition|)
block|{
comment|// we have to send out a value the node will understand
name|version
operator|=
name|MATCH_ANY_PRE_1_2_0
expr_stmt|;
block|}
name|out
operator|.
name|writeVLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
DECL|method|readVersionWithVLongForBW
specifier|public
specifier|static
name|long
name|readVersionWithVLongForBW
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
condition|)
block|{
return|return
name|in
operator|.
name|readLong
argument_list|()
return|;
block|}
else|else
block|{
name|long
name|version
init|=
name|in
operator|.
name|readVLong
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|==
name|MATCH_ANY_PRE_1_2_0
condition|)
block|{
return|return
name|MATCH_ANY
return|;
block|}
return|return
name|version
return|;
block|}
block|}
DECL|method|Versions
specifier|private
name|Versions
parameter_list|()
block|{     }
comment|/** Wraps an {@link LeafReaderContext}, a doc ID<b>relative to the context doc base</b> and a version. */
DECL|class|DocIdAndVersion
specifier|public
specifier|static
class|class
name|DocIdAndVersion
block|{
DECL|field|docId
specifier|public
specifier|final
name|int
name|docId
decl_stmt|;
DECL|field|version
specifier|public
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|context
specifier|public
specifier|final
name|LeafReaderContext
name|context
decl_stmt|;
DECL|method|DocIdAndVersion
specifier|public
name|DocIdAndVersion
parameter_list|(
name|int
name|docId
parameter_list|,
name|long
name|version
parameter_list|,
name|LeafReaderContext
name|context
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
block|}
block|}
comment|/**      * Load the internal doc ID and version for the uid from the reader, returning<ul>      *<li>null if the uid wasn't found,      *<li>a doc ID and a version otherwise, the version being potentially set to {@link #NOT_SET} if the uid has no associated version      *</ul>      */
DECL|method|loadDocIdAndVersion
specifier|public
specifier|static
name|DocIdAndVersion
name|loadDocIdAndVersion
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Term
name|term
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|term
operator|.
name|field
argument_list|()
operator|.
name|equals
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
assert|;
return|return
name|getLookupState
argument_list|(
name|reader
argument_list|)
operator|.
name|lookup
argument_list|(
name|term
operator|.
name|bytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Load the version for the uid from the reader, returning<ul>      *<li>{@link #NOT_FOUND} if no matching doc exists,      *<li>{@link #NOT_SET} if no version is available,      *<li>the version associated with the provided uid otherwise      *</ul>      */
DECL|method|loadVersion
specifier|public
specifier|static
name|long
name|loadVersion
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Term
name|term
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|DocIdAndVersion
name|docIdAndVersion
init|=
name|loadDocIdAndVersion
argument_list|(
name|reader
argument_list|,
name|term
argument_list|)
decl_stmt|;
return|return
name|docIdAndVersion
operator|==
literal|null
condition|?
name|NOT_FOUND
else|:
name|docIdAndVersion
operator|.
name|version
return|;
block|}
block|}
end_class

end_unit

