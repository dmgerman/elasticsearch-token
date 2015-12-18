begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
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

begin_comment
comment|/**  * Version 0 of the translog format, there is no header in this file  */
end_comment

begin_class
annotation|@
name|Deprecated
DECL|class|LegacyTranslogReader
specifier|public
specifier|final
class|class
name|LegacyTranslogReader
extends|extends
name|LegacyTranslogReaderBase
block|{
comment|/**      * Create a snapshot of translog file channel. The length parameter should be consistent with totalOperations and point      * at the end of the last operation in this snapshot.      */
DECL|method|LegacyTranslogReader
name|LegacyTranslogReader
parameter_list|(
name|long
name|generation
parameter_list|,
name|ChannelReference
name|channelReference
parameter_list|,
name|long
name|fileLength
parameter_list|)
block|{
name|super
argument_list|(
name|generation
argument_list|,
name|channelReference
argument_list|,
literal|0
argument_list|,
name|fileLength
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|protected
name|Translog
operator|.
name|Operation
name|read
parameter_list|(
name|BufferedChecksumStreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// read the opsize before an operation.
comment|// Note that this was written& read out side of the stream when this class was used, but it makes things more consistent
comment|// to read this here
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|Translog
operator|.
name|Operation
operator|.
name|Type
name|type
init|=
name|Translog
operator|.
name|Operation
operator|.
name|Type
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|Translog
operator|.
name|Operation
name|operation
init|=
name|Translog
operator|.
name|newOperationFromType
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|operation
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|operation
return|;
block|}
annotation|@
name|Override
DECL|method|newReader
specifier|protected
name|ImmutableTranslogReader
name|newReader
parameter_list|(
name|long
name|generation
parameter_list|,
name|ChannelReference
name|channelReference
parameter_list|,
name|long
name|firstOperationOffset
parameter_list|,
name|long
name|length
parameter_list|,
name|int
name|totalOperations
parameter_list|)
block|{
assert|assert
name|totalOperations
operator|==
operator|-
literal|1
operator|:
literal|"expected unknown but was: "
operator|+
name|totalOperations
assert|;
assert|assert
name|firstOperationOffset
operator|==
literal|0
assert|;
return|return
operator|new
name|LegacyTranslogReader
argument_list|(
name|generation
argument_list|,
name|channelReference
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

