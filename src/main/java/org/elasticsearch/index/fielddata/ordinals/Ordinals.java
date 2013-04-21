begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.ordinals
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
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
name|util
operator|.
name|IntsRef
import|;
end_import

begin_comment
comment|/**  * A thread safe ordinals abstraction. Ordinals can only be positive integers.  */
end_comment

begin_interface
DECL|interface|Ordinals
specifier|public
interface|interface
name|Ordinals
block|{
comment|/**      * Are the ordinals backed by a single ordinals array?      */
DECL|method|hasSingleArrayBackingStorage
name|boolean
name|hasSingleArrayBackingStorage
parameter_list|()
function_decl|;
comment|/**      * Returns the backing storage for this ordinals.      */
DECL|method|getBackingStorage
name|Object
name|getBackingStorage
parameter_list|()
function_decl|;
comment|/**      * The memory size this ordinals take.      */
DECL|method|getMemorySizeInBytes
name|long
name|getMemorySizeInBytes
parameter_list|()
function_decl|;
comment|/**      * Is one of the docs maps to more than one ordinal?      */
DECL|method|isMultiValued
name|boolean
name|isMultiValued
parameter_list|()
function_decl|;
comment|/**      * The number of docs in this ordinals.      */
DECL|method|getNumDocs
name|int
name|getNumDocs
parameter_list|()
function_decl|;
comment|/**      * The number of ordinals, excluding the "0" ordinal indicating a missing value.      */
DECL|method|getNumOrds
name|int
name|getNumOrds
parameter_list|()
function_decl|;
comment|/**      * Returns total unique ord count; this includes +1 for      * the null ord (always 0).      */
DECL|method|getMaxOrd
name|int
name|getMaxOrd
parameter_list|()
function_decl|;
comment|/**      * Returns a lightweight (non thread safe) view iterator of the ordinals.      */
DECL|method|ordinals
name|Docs
name|ordinals
parameter_list|()
function_decl|;
comment|/**      * A non thread safe ordinals abstraction, yet very lightweight to create. The idea      * is that this gets created for each "iteration" over ordinals.      *<p/>      *<p>A value of 0 ordinal when iterating indicated "no" value.</p>      */
DECL|interface|Docs
interface|interface
name|Docs
block|{
comment|/**          * Returns the original ordinals used to generate this Docs "itereator".          */
DECL|method|ordinals
name|Ordinals
name|ordinals
parameter_list|()
function_decl|;
comment|/**          * The number of docs in this ordinals.          */
DECL|method|getNumDocs
name|int
name|getNumDocs
parameter_list|()
function_decl|;
comment|/**          * The number of ordinals, excluding the "0" ordinal (indicating a missing value).          */
DECL|method|getNumOrds
name|int
name|getNumOrds
parameter_list|()
function_decl|;
comment|/**          * Returns total unique ord count; this includes +1 for          * the null ord (always 0).          */
DECL|method|getMaxOrd
name|int
name|getMaxOrd
parameter_list|()
function_decl|;
comment|/**          * Is one of the docs maps to more than one ordinal?          */
DECL|method|isMultiValued
name|boolean
name|isMultiValued
parameter_list|()
function_decl|;
comment|/**          * The ordinal that maps to the relevant docId. If it has no value, returns          *<tt>0</tt>.          */
DECL|method|getOrd
name|int
name|getOrd
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
comment|/**          * Returns an array of ordinals matching the docIds, with 0 length one          * for a doc with no ordinals.          */
DECL|method|getOrds
name|IntsRef
name|getOrds
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
comment|/**          * Returns an iterator of the ordinals that match the docId, with an          * empty iterator for a doc with no ordinals.          */
DECL|method|getIter
name|Iter
name|getIter
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
comment|/**          * An iterator over ordinals values.          */
DECL|interface|Iter
interface|interface
name|Iter
block|{
comment|/**              * Gets the next ordinal. Returning 0 if the iteration is exhausted.              */
DECL|method|next
name|int
name|next
parameter_list|()
function_decl|;
block|}
DECL|class|EmptyIter
specifier|static
class|class
name|EmptyIter
implements|implements
name|Iter
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
name|EmptyIter
name|INSTANCE
init|=
operator|new
name|EmptyIter
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|next
specifier|public
name|int
name|next
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
DECL|class|SingleValueIter
specifier|static
class|class
name|SingleValueIter
implements|implements
name|Iter
block|{
DECL|field|value
specifier|private
name|int
name|value
decl_stmt|;
DECL|method|reset
specifier|public
name|SingleValueIter
name|reset
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|int
name|next
parameter_list|()
block|{
name|int
name|actual
init|=
name|value
decl_stmt|;
name|value
operator|=
literal|0
expr_stmt|;
return|return
name|actual
return|;
block|}
block|}
block|}
block|}
end_interface

end_unit

