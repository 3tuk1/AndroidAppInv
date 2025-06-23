package com.inv.inventryapp.usecase;

import com.inv.inventryapp.model.entity.Analysis;
import com.inv.inventryapp.model.entity.History;
import com.inv.inventryapp.model.entity.Product;
import com.inv.inventryapp.repository.AnalysisRepository;
import com.inv.inventryapp.repository.HistoryRepository;
import com.inv.inventryapp.repository.ProductRepository;
import com.inv.inventryapp.repository.ShoppingListRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConsumptionAnalysisUseCaseTest {

    // テスト対象クラス
    private ConsumptionAnalysisUseCase consumptionAnalysisUseCase;

    // 依存クラスのモック
    @Mock
    private ProductRepository productRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private AnalysisRepository analysisRepository;
    @Mock
    private ShoppingListRepository shoppingListRepository;

    // メソッドに渡された引数をキャプチャするためのCaptor
    @Captor
    private ArgumentCaptor<Analysis> analysisArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        consumptionAnalysisUseCase = new ConsumptionAnalysisUseCase(
                productRepository,
                historyRepository,
                analysisRepository,
                shoppingListRepository
        );
    }

    /**
     * 正常系のテストケース
     * 十分な消費履歴がある場合に、分析結果が正しく計算され保存されることを確認します。
     */
    @Test
    public void analyzeAndSave_successPath_calculatesAndSavesAnalysis() {
        // GIVEN
        Product testProduct = new Product();
        testProduct.setProductName("テスト牛乳");
        testProduct.setQuantity(10);
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.getAllProductsList()).thenReturn(products);

        LocalDate today = LocalDate.now();
        History history1 = new History("テスト牛乳", "消費", today.minusDays(30), 3);
        History history2 = new History("テスト牛乳", "消費", today.minusDays(15), 2);
        History history3 = new History("テスト牛乳", "消費", today, 1);
        List<History> histories = List.of(history1, history2, history3);
        when(historyRepository.getAllHistories()).thenReturn(histories);
        when(analysisRepository.findByProductName("テスト牛乳")).thenReturn(null);

        // WHEN
        consumptionAnalysisUseCase.analyzeAndSave();

        // THEN
        verify(analysisRepository, times(1)).addAnalysis(analysisArgumentCaptor.capture());
        Analysis capturedAnalysis = analysisArgumentCaptor.getValue();
        assertEquals("テスト牛乳", capturedAnalysis.getProductName());

        float expectedDaysPerItem = 5.0f;
        float expectedRemainingDays = 50.0f;
        LocalDate expectedOutOfStockDate = today.plusDays(50);
        int expectedRecommendedQuantity = 6;
        float expectedPriorityScore = 2.0f;

        assertEquals(expectedDaysPerItem, capturedAnalysis.getDaysPerItem(), 0.01);
        assertEquals(expectedRemainingDays, capturedAnalysis.getRemainingDays(), 0.01);
        assertEquals(expectedOutOfStockDate, capturedAnalysis.getOutOfStockDate());
        assertEquals(expectedRecommendedQuantity, capturedAnalysis.getRecommendedQuantity());
        assertEquals(expectedPriorityScore, capturedAnalysis.getPriorityScore(), 0.01);
    }

    /**
     * 異常系のテストケース
     * 分析に必要な消費履歴が足りない（1件しかない）場合、分析処理が実行されないことを確認します。
     */
    @Test
    public void analyzeAndSave_notEnoughHistory_doesNotSaveAnalysis() {
        // GIVEN
        Product testProduct = new Product();
        testProduct.setProductName("テストジュース");
        testProduct.setQuantity(5);
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.getAllProductsList()).thenReturn(products);

        History history1 = new History("テストジュース", "消費", LocalDate.now(), 2);
        List<History> histories = Collections.singletonList(history1);
        when(historyRepository.getAllHistories()).thenReturn(histories);

        // WHEN
        consumptionAnalysisUseCase.analyzeAndSave();

        // THEN
        verify(analysisRepository, never()).addAnalysis(any(Analysis.class));
    }

    /**
     * 異常系のテストケース
     * 消費履歴の期間が0日（全て同日）の場合、分析処理が実行されないことを確認します。
     */
    @Test
    public void analyzeAndSave_zeroDaysBetween_doesNotSaveAnalysis() {
        // GIVEN
        Product testProduct = new Product();
        testProduct.setProductName("テストウォーター");
        testProduct.setQuantity(2);
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.getAllProductsList()).thenReturn(products);

        LocalDate sameDay = LocalDate.now();
        History history1 = new History("テストウォーター", "消費", sameDay, 1);
        History history2 = new History("テストウォーター", "消費", sameDay, 1);
        List<History> histories = List.of(history1, history2);
        when(historyRepository.getAllHistories()).thenReturn(histories);

        // WHEN
        consumptionAnalysisUseCase.analyzeAndSave();

        // THEN
        verify(analysisRepository, never()).addAnalysis(any(Analysis.class));
    }

    // --- ▼▼▼ ここからが追加されたテストケース ▼▼▼ ---

    /**
     * 購入リスト連携のテストケース
     * 在庫が今月中になくなると予測された場合、購入リストに追加されることを確認します。
     */
    @Test
    public void analyzeAndSave_itemRunningOutThisMonth_isAddedToShoppingList() {
        // --- GIVEN ---
        Product testProduct = new Product();
        testProduct.setProductName("テストヨーグルト");
        testProduct.setQuantity(2); // 在庫は2個
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.getAllProductsList()).thenReturn(products);

        // 10日間で4個消費 -> 1日あたり0.4個消費
        // 残り日数: 2個 / 0.4個/日 = 5日 -> 今月中に在庫切れ
        LocalDate today = LocalDate.now();
        History history1 = new History("テストヨーグルト", "消費", today.minusDays(10), 2);
        History history2 = new History("テストヨーグルト", "消費", today, 2);
        List<History> histories = List.of(history1, history2);
        when(historyRepository.getAllHistories()).thenReturn(histories);
        when(analysisRepository.findByProductName("テストヨーグルト")).thenReturn(null);

        // --- WHEN ---
        consumptionAnalysisUseCase.analyzeAndSave();

        // --- THEN ---
        // 1. 分析結果から推奨購入数を取得
        verify(analysisRepository).addAnalysis(analysisArgumentCaptor.capture());
        Analysis capturedAnalysis = analysisArgumentCaptor.getValue();
        int expectedQuantity = capturedAnalysis.getRecommendedQuantity();

        // 2. shoppingListRepository.addShoppingList が正しい引数で1回呼ばれたことを確認
        verify(shoppingListRepository, times(1)).addShoppingList(eq("テストヨーグルト"), eq(expectedQuantity));
    }

    /**
     * 購入リスト連携のテストケース（追加なし）
     * 在庫が来月以降になくなると予測された場合、購入リストに追加されないことを確認します。
     */
    @Test
    public void analyzeAndSave_itemRunningOutNextMonth_isNotAddedToShoppingList() {
        // --- GIVEN ---
        Product testProduct = new Product();
        testProduct.setProductName("テストチーズ");
        testProduct.setQuantity(40); // 在庫は40個
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.getAllProductsList()).thenReturn(products);

        // 20日間で10個消費 -> 1日あたり0.5個消費
        // 残り日数: 40個 / 0.5個/日 = 80日 -> 来月以降に在庫切れ
        LocalDate today = LocalDate.now();
        History history1 = new History("テストチーズ", "消費", today.minusDays(20), 5);
        History history2 = new History("テストチーズ", "消費", today, 5);
        List<History> histories = List.of(history1, history2);
        when(historyRepository.getAllHistories()).thenReturn(histories);
        when(analysisRepository.findByProductName("テストチーズ")).thenReturn(null);

        // --- WHEN ---
        consumptionAnalysisUseCase.analyzeAndSave();

        // --- THEN ---
        // shoppingListRepository.addShoppingList が一度も呼ばれないことを確認
        verify(shoppingListRepository, never()).addShoppingList(anyString(), anyInt());
    }
}