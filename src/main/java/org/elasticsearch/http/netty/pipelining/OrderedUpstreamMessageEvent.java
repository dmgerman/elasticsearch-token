begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.http.netty.pipelining
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|pipelining
package|;
end_package

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|UpstreamMessageEvent
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketAddress
import|;
end_import

begin_comment
comment|/**  * Permits upstream message events to be ordered.  *  * @author Christopher Hunt  */
end_comment

begin_class
DECL|class|OrderedUpstreamMessageEvent
specifier|public
class|class
name|OrderedUpstreamMessageEvent
extends|extends
name|UpstreamMessageEvent
block|{
DECL|field|sequence
specifier|final
name|int
name|sequence
decl_stmt|;
DECL|method|OrderedUpstreamMessageEvent
specifier|public
name|OrderedUpstreamMessageEvent
parameter_list|(
specifier|final
name|int
name|sequence
parameter_list|,
specifier|final
name|Channel
name|channel
parameter_list|,
specifier|final
name|Object
name|msg
parameter_list|,
specifier|final
name|SocketAddress
name|remoteAddress
parameter_list|)
block|{
name|super
argument_list|(
name|channel
argument_list|,
name|msg
argument_list|,
name|remoteAddress
argument_list|)
expr_stmt|;
name|this
operator|.
name|sequence
operator|=
name|sequence
expr_stmt|;
block|}
DECL|method|getSequence
specifier|public
name|int
name|getSequence
parameter_list|()
block|{
return|return
name|sequence
return|;
block|}
block|}
end_class

end_unit

