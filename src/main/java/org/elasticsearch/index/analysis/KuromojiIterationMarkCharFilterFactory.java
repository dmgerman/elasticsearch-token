begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
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
name|analysis
operator|.
name|ja
operator|.
name|JapaneseIterationMarkCharFilter
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
name|inject
operator|.
name|Inject
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
name|inject
operator|.
name|assistedinject
operator|.
name|Assisted
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
name|settings
operator|.
name|Settings
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
name|Index
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_class
DECL|class|KuromojiIterationMarkCharFilterFactory
specifier|public
class|class
name|KuromojiIterationMarkCharFilterFactory
extends|extends
name|AbstractCharFilterFactory
block|{
DECL|field|normalizeKanji
specifier|private
specifier|final
name|boolean
name|normalizeKanji
decl_stmt|;
DECL|field|normalizeKana
specifier|private
specifier|final
name|boolean
name|normalizeKana
decl_stmt|;
annotation|@
name|Inject
DECL|method|KuromojiIterationMarkCharFilterFactory
specifier|public
name|KuromojiIterationMarkCharFilterFactory
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
annotation|@
name|Assisted
name|String
name|name
parameter_list|,
annotation|@
name|Assisted
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|normalizeKanji
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"normalize_kanji"
argument_list|,
name|JapaneseIterationMarkCharFilter
operator|.
name|NORMALIZE_KANJI_DEFAULT
argument_list|)
expr_stmt|;
name|normalizeKana
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"normalize_kana"
argument_list|,
name|JapaneseIterationMarkCharFilter
operator|.
name|NORMALIZE_KANA_DEFAULT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|Reader
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|)
block|{
return|return
operator|new
name|JapaneseIterationMarkCharFilter
argument_list|(
name|reader
argument_list|,
name|normalizeKanji
argument_list|,
name|normalizeKana
argument_list|)
return|;
block|}
block|}
end_class

end_unit

