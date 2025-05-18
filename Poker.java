import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Poker {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)) { //A J Q K
                if (value == "A") {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value); //2-10
        }

        public boolean isAce() {
            return value == "A";
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random(); //shuffle deck

    //dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    //window
    int boardWidth = 900;
    int boardHeight = 800;

    int cardWidth = 110; //ratio should 1/1.4
    int cardHeight = 154;

    // Poker (community cards)
    ArrayList<Card> communityCards = new ArrayList<>();
    int revealedCommunity = 0; // จำนวนไพ่ที่เปิดแล้ว

    // coin system
    int playerCoin = 1000;
    int dealerCoin = 1000;
    int currentBet = 0;
    boolean canRaise = true; // สามารถ raise ได้ก่อนเปิด flop

    // เพิ่มตัวแปรสำหรับตำแหน่ง Dealer/BB
    boolean isPlayerDealer = true; // true = player เป็น Dealer, false = dealer เป็น Dealer
    int smallBlind = 10;
    int bigBlind = 20;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                // วาดไพ่กลาง (community cards) แบบโป๊กเกอร์
                int communityY = getHeight() / 2 - cardHeight / 2;
                int totalWidth = 5 * cardWidth + 4 * 10;
                int communityXStart = (getWidth() - totalWidth) / 2;
                for (int i = 0; i < revealedCommunity; i++) {
                    Card card = communityCards.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, communityXStart + i * (cardWidth + 10), communityY, cardWidth, cardHeight, null);
                }
                // วาดหลังไพ่สำหรับใบที่ยังไม่เปิด
                for (int i = revealedCommunity; i < 5; i++) {
                    Image backImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                    g.drawImage(backImg, communityXStart + i * (cardWidth + 10), communityY, cardWidth, cardHeight, null);
                }

                // Center dealer's hidden card
                int dealerY = 20;
                int dealerXStart = (getWidth() - (dealerHand.size() + 1) * (cardWidth + 5)) / 2;
                // วาดหลังไพ่ dealer ทั้งหมด (ปิดหน้าไพ่ dealer ตลอดเกม)
                Image backImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                g.drawImage(backImg, dealerXStart, dealerY, cardWidth, cardHeight, null);
                for (int i = 0; i < dealerHand.size(); i++) {
                    g.drawImage(backImg, dealerXStart + (cardWidth + 5) * (i + 1), dealerY, cardWidth, cardHeight, null);
                }

                // Center player's hand at the bottom
                int playerY = getHeight() - cardHeight - 40; // ขยับลงล่างสุด (40 คือ margin จากขอบล่าง)
                int playerXStart = (getWidth() - playerHand.size() * (cardWidth + 5)) / 2;
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, playerXStart + (cardWidth + 5) * i, playerY, cardWidth, cardHeight, null);
                }
                

                // if (!restartButton.isEnabled()) {
                //     dealerSum = reduceDealerAce();
                //     playerSum = reducePlayerAce();
                //     System.out.println("STAY: ");
                //     System.out.println(dealerSum);
                //     System.out.println(playerSum);

                //     String message = "";
                //     if (playerSum > 21) {
                //         message = "You Lose!";
                //     }
                //     else if (dealerSum > 21) {
                //         message = "You Win!";
                //     }
                //     //both you and dealer <= 21
                //     else if (playerSum == dealerSum) {
                //         message = "Tie!";
                //     }
                //     else if (playerSum > dealerSum) {
                //         message = "You Win!";
                //     }
                //     else if (playerSum < dealerSum) {
                //         message = "You Lose!";
                //     }

                //     g.setFont(new Font("Arial", Font.PLAIN, 30));
                //     g.setColor(Color.white);
                //     g.drawString(message, getWidth()/2 - 60, 250);
                // }

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                g.drawString("Player Coin: " + playerCoin, 30, getHeight() - 50);
                g.drawString("Dealer Coin: " + dealerCoin, 30, 50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    // เปลี่ยนชื่อปุ่มให้ตรงกับ Poker จริง ๆ
    JButton raiseButton = new JButton("Raise");
    JButton callButton = new JButton("Call");
    JButton foldButton = new JButton("Fold");
    JButton restartButton = new JButton("Restart");

    JLabel playerCoinLabel = new JLabel();
    JLabel dealerCoinLabel = new JLabel();

    Poker() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        // Coin labels
        playerCoinLabel.setText("Player Coin: " + playerCoin);
        dealerCoinLabel.setText("Dealer Coin: " + dealerCoin);
        playerCoinLabel.setForeground(Color.WHITE);
        dealerCoinLabel.setForeground(Color.WHITE);
        gamePanel.setLayout(null);
        playerCoinLabel.setBounds(30, boardHeight - 80, 200, 30);
        dealerCoinLabel.setBounds(30, 20, 200, 30);
        gamePanel.add(playerCoinLabel);
        gamePanel.add(dealerCoinLabel);

        // Button panel (ใช้ปุ่มใหม่)
        buttonPanel.add(raiseButton);
        buttonPanel.add(callButton);
        buttonPanel.add(foldButton);
        buttonPanel.add(restartButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // เริ่มต้นรอบใหม่ เปิดเฉพาะปุ่ม Raise
        enableActionButtons(true, false, false);

         restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // รีเซ็ตเงินถ้าต้องการ
                playerCoin = 1000;
                dealerCoin = 1000;
                startGame();
            }
        });

        // Restart
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // รีเซ็ตเงินถ้าต้องการ
                playerCoin = 1000;
                dealerCoin = 1000;
                startGame();
            }
        });

        // Raise
        raiseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int raiseAmount = 100;
                if (canRaise && playerCoin >= raiseAmount && dealerCoin >= raiseAmount) {
                    playerCoin -= raiseAmount;
                    dealerCoin -= raiseAmount;
                    currentBet += 2 * raiseAmount;
                    updateCoinLabels();
                    canRaise = false;
                    enableActionButtons(false, true, true); // เปิด call กับ fold
                    botAction(); // ให้ bot ตัดสินใจต่อ
                }
            }
        });

       
        

        // Call
        callButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // เปิด community card ถัดไป
                if (revealedCommunity < 3) {
                    revealedCommunity = 3;
                } else if (revealedCommunity < 5) {
                    revealedCommunity++;
                }
                // ปิดปุ่ม call/fold เมื่อเปิดครบ 5 ใบ
                if (revealedCommunity >= 5) {
                    enableActionButtons(false, false, false);
                } else {
                    enableActionButtons(true, false, false); // ให้ raise ได้รอบถัดไป
                }
                gamePanel.repaint();
                botAction(); // ให้ bot ตัดสินใจต่อ
            }
        });

       

        // Fold
        foldButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ผู้เล่นหมอบ dealer ชนะ
                dealerCoin += currentBet;
                updateCoinLabels();
                enableActionButtons(false, false, false);
                JOptionPane.showMessageDialog(frame, "You folded! Dealer wins.");
            }
        });

        gamePanel.repaint();
    }

    // ฟังก์ชันช่วยเปิด/ปิดปุ่ม
    private void enableActionButtons(boolean raise, boolean call, boolean fold) {
        raiseButton.setEnabled(raise);
        callButton.setEnabled(call);
        foldButton.setEnabled(fold);
    }

    public void updateCoinLabels() {
        playerCoinLabel.setText("Player Coin: " + playerCoin);
        dealerCoinLabel.setText("Dealer Coin: " + dealerCoin);
    }

    public void startGame() {
        buildDeck();
        shuffleDeck();

        // Reset สถานะ
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        // แจกไพ่ dealer
        hiddenCard = deck.remove(deck.size()-1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;
        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        // แจกไพ่ player
        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        // แจกไพ่กลาง 5 ใบ
        communityCards.clear();
        for (int i = 0; i < 5; i++) {
            communityCards.add(deck.remove(deck.size()-1));
        }
        revealedCommunity = 0;
        canRaise = true;
        currentBet = 0;

        // วาง Blind
        if (isPlayerDealer) {
            playerCoin -= smallBlind;
            dealerCoin -= bigBlind;
            currentBet = bigBlind;
        } else {
            dealerCoin -= smallBlind;
            playerCoin -= bigBlind;
            currentBet = bigBlind;
        }

        // สลับตำแหน่ง Dealer/BB สำหรับรอบถัดไป
        isPlayerDealer = !isPlayerDealer;

        enableActionButtons(true, false, false);
        updateCoinLabels();
        gamePanel.repaint();
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }

        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

    // ฟังก์ชันให้ bot ตัดสินใจ raise หรือ fold แบบสุ่ม (50/50)
    private void botAction() {
        if (dealerCoin < currentBet) {
            // ถ้าเงินไม่พอให้ fold
            foldBot();
            return;
        }
        if (Math.random() < 0.5 && dealerCoin >= 100) {
            // Bot raise
            int raiseAmount = 100;
            dealerCoin -= raiseAmount;
            currentBet += raiseAmount;
            updateCoinLabels();
            JOptionPane.showMessageDialog(frame, "Bot (Dealer) raises!");
            // เปิดปุ่ม Call/Fold ให้ผู้เล่นตอบสนอง
            enableActionButtons(false, true, true);
        } else {
            // Bot fold
            foldBot();
        }
    }

    // ฟังก์ชันช่วยให้ bot fold
    private void foldBot() {
        playerCoin += currentBet;
        updateCoinLabels();
        enableActionButtons(false, false, false);
        JOptionPane.showMessageDialog(frame, "Bot (Dealer) folds! You win.");
    }

    // ฟังก์ชันสำหรับเริ่มรอบเดิมพันใหม่ (หลังเปิดไพ่กลางแต่ละใบ)
   
}
