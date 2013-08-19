begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastShardOperationRequest
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
name|index
operator|.
name|shard
operator|.
name|ShardId
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
comment|/**  */
end_comment

begin_class
DECL|class|PercolateShardRequest
specifier|public
class|class
name|PercolateShardRequest
extends|extends
name|BroadcastShardOperationRequest
block|{
DECL|field|documentType
specifier|private
name|String
name|documentType
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|docSource
specifier|private
name|BytesReference
name|docSource
decl_stmt|;
DECL|field|onlyCount
specifier|private
name|boolean
name|onlyCount
decl_stmt|;
DECL|method|PercolateShardRequest
specifier|public
name|PercolateShardRequest
parameter_list|()
block|{     }
DECL|method|PercolateShardRequest
specifier|public
name|PercolateShardRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|PercolateShardRequest
specifier|public
name|PercolateShardRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|PercolateRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|documentType
operator|=
name|request
operator|.
name|documentType
argument_list|()
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|request
operator|.
name|source
argument_list|()
expr_stmt|;
name|this
operator|.
name|docSource
operator|=
name|request
operator|.
name|docSource
argument_list|()
expr_stmt|;
name|this
operator|.
name|onlyCount
operator|=
name|request
operator|.
name|onlyCount
argument_list|()
expr_stmt|;
block|}
DECL|method|PercolateShardRequest
specifier|public
name|PercolateShardRequest
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|PercolateRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|documentType
operator|=
name|request
operator|.
name|documentType
argument_list|()
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|request
operator|.
name|source
argument_list|()
expr_stmt|;
name|this
operator|.
name|docSource
operator|=
name|request
operator|.
name|docSource
argument_list|()
expr_stmt|;
name|this
operator|.
name|onlyCount
operator|=
name|request
operator|.
name|onlyCount
argument_list|()
expr_stmt|;
block|}
DECL|method|documentType
specifier|public
name|String
name|documentType
parameter_list|()
block|{
return|return
name|documentType
return|;
block|}
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|docSource
specifier|public
name|BytesReference
name|docSource
parameter_list|()
block|{
return|return
name|docSource
return|;
block|}
DECL|method|onlyCount
specifier|public
name|boolean
name|onlyCount
parameter_list|()
block|{
return|return
name|onlyCount
return|;
block|}
DECL|method|documentType
name|void
name|documentType
parameter_list|(
name|String
name|documentType
parameter_list|)
block|{
name|this
operator|.
name|documentType
operator|=
name|documentType
expr_stmt|;
block|}
DECL|method|source
name|void
name|source
parameter_list|(
name|BytesReference
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
DECL|method|docSource
name|void
name|docSource
parameter_list|(
name|BytesReference
name|docSource
parameter_list|)
block|{
name|this
operator|.
name|docSource
operator|=
name|docSource
expr_stmt|;
block|}
DECL|method|onlyCount
name|void
name|onlyCount
parameter_list|(
name|boolean
name|onlyCount
parameter_list|)
block|{
name|this
operator|.
name|onlyCount
operator|=
name|onlyCount
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|documentType
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|docSource
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|onlyCount
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|documentType
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|docSource
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|onlyCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

