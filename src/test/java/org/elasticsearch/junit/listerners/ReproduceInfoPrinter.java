begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.junit.listerners
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|junit
operator|.
name|listerners
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedContext
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|ReproduceErrorMessageBuilder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|TraceFormatting
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|internal
operator|.
name|AssumptionViolatedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|notification
operator|.
name|Failure
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|notification
operator|.
name|RunListener
import|;
end_import

begin_comment
comment|/**  * A {@link RunListener} that emits to {@link System#err} a string with command  * line parameters allowing quick test re-run under ANT command line.  */
end_comment

begin_class
DECL|class|ReproduceInfoPrinter
specifier|public
class|class
name|ReproduceInfoPrinter
extends|extends
name|RunListener
block|{
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ElasticsearchTestCase
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|testStarted
specifier|public
name|void
name|testStarted
parameter_list|(
name|Description
name|description
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Test {} started"
argument_list|,
name|description
operator|.
name|getDisplayName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testFailure
specifier|public
name|void
name|testFailure
parameter_list|(
name|Failure
name|failure
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Ignore assumptions.
if|if
condition|(
name|failure
operator|.
name|getException
argument_list|()
operator|instanceof
name|AssumptionViolatedException
condition|)
block|{
return|return;
block|}
specifier|final
name|Description
name|d
init|=
name|failure
operator|.
name|getDescription
argument_list|()
decl_stmt|;
specifier|final
name|StringBuilder
name|b
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|"FAILURE  : "
argument_list|)
operator|.
name|append
argument_list|(
name|d
operator|.
name|getDisplayName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|"REPRODUCE WITH  : mvn test"
argument_list|)
expr_stmt|;
operator|new
name|MavenMessageBuilder
argument_list|(
name|b
argument_list|)
operator|.
name|appendAllOpts
argument_list|(
name|failure
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|"Throwable:\n"
argument_list|)
expr_stmt|;
if|if
condition|(
name|failure
operator|.
name|getException
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|TraceFormatting
name|traces
init|=
operator|new
name|TraceFormatting
argument_list|()
decl_stmt|;
try|try
block|{
name|traces
operator|=
name|RandomizedContext
operator|.
name|current
argument_list|()
operator|.
name|getRunner
argument_list|()
operator|.
name|getTraceFormatting
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// Ignore if no context.
block|}
name|traces
operator|.
name|formatThrowable
argument_list|(
name|b
argument_list|,
name|failure
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|error
argument_list|(
name|b
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|MavenMessageBuilder
specifier|private
specifier|static
class|class
name|MavenMessageBuilder
extends|extends
name|ReproduceErrorMessageBuilder
block|{
DECL|method|MavenMessageBuilder
specifier|public
name|MavenMessageBuilder
parameter_list|(
name|StringBuilder
name|b
parameter_list|)
block|{
name|super
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
comment|/**          * Append a single VM option.          */
DECL|method|appendOpt
specifier|public
name|ReproduceErrorMessageBuilder
name|appendOpt
parameter_list|(
name|String
name|sysPropName
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|sysPropName
operator|.
name|equals
argument_list|(
literal|"tests.iters"
argument_list|)
condition|)
block|{
comment|// we don't want the iters to be in there!
return|return
name|this
return|;
block|}
return|return
name|super
operator|.
name|appendOpt
argument_list|(
name|sysPropName
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

